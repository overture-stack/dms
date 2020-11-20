package bio.overture.dms.infra.service;

import bio.overture.dms.core.util.Joiner;
import bio.overture.dms.core.util.Splitter;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Mount;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static bio.overture.dms.core.util.Strings.isBlank;
import static com.github.dockerjava.api.model.MountType.BIND;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Builder
@RequiredArgsConstructor
public class DockerComposeClient {

  /**
   * Constants
   */
  private static final Mount DOCKER_SOCK_MOUNT = new Mount()
      .withType(BIND)
      .withSource("/var/run/docker.sock")
      .withTarget("/var/run/docker.sock")
      .withReadOnly(true);

  private static final Mount DOCKER_EXEC_MOUNT = new Mount()
      .withType(BIND)
      .withSource("/usr/bin/docker")
      .withTarget("/usr/bin/docker")
      .withReadOnly(true);

  private static final HostConfig HOST_CONFIG = HostConfig.newHostConfig()
      .withMounts(List.of(DOCKER_SOCK_MOUNT, DOCKER_EXEC_MOUNT));

  /**
   * Configs
   */
  @NonNull private final String projectName;
  @NonNull private final String dockerComposeContainerName;
  @NonNull private final String dockerComposeImageName;

  /**
   * Dependencies
   */
  @NonNull private final DockerClient dockerClient;

  public DCResponse runCommand(@NonNull File dockerComposeFile, @NonNull String command) throws InterruptedException {
    pullImage();
    removeExistingContainer();
    val containerId = createContainer(command);
    copyDockerComposeFileToContainer(dockerComposeFile, containerId);
    runContainer(containerId);
    val out = getContainerLogs(containerId);

    removeContainer(containerId);
    return out;
  }

  @SneakyThrows
  private void pullImage(){
    findImage(dockerComposeImageName)
        .orElseGet(() -> {
          internalPullImage(dockerComposeImageName);
          return null;
        });
  }

  @SneakyThrows
  private void internalPullImage(String imageName){
    dockerClient.pullImageCmd(dockerComposeImageName).start().awaitCompletion();
  }

  private Optional<Image> findImage(String imageName) {
    return dockerClient.listImagesCmd().exec().stream()
        .filter(x -> matchImageName(x, imageName))
        .findFirst();
  }

  private static boolean matchImageName(Image image, String imageName) {
    return image.getId().replaceAll("^\\S+:", "").startsWith(imageName) || matchTag(image.getRepoTags(), imageName);
  }

  private static boolean matchTag(String[] repoTags, String imageName) {
    return asList(repoTags).contains(imageName);
  }

  public static String resolveRepoTag(String repo, String tag) {
    return repo + ":" + tag;
  }

  private void removeExistingContainer(){
    dockerClient.listContainersCmd()
        .withLimit(1)
        .withNameFilter(List.of(dockerComposeContainerName))
        .exec()
        .stream()
        .findFirst()
        .ifPresent(x -> dockerClient.removeContainerCmd(x.getId())
            .withForce(true)
            .exec());
  }

  private String createContainer(String command){
    val splitCommands = Stream.concat(Stream.of("/docker-compose","--log-level=CRITICAL", "-p",  projectName, "-f", "/docker-compose.yaml", "--no-ansi"),
        Splitter.WHITESPACE.splitStream(command))
        .collect(toUnmodifiableList());
    log.info("Running docker-compose command: '{}'", Joiner.WHITESPACE.join(splitCommands));

    return dockerClient.createContainerCmd(dockerComposeImageName)
        .withName(dockerComposeContainerName)
        .withCmd(splitCommands)
        .withHostConfig(HOST_CONFIG)
        .exec()
        .getId();
  }

  private void copyDockerComposeFileToContainer(File dockerComposeFile, String containerId){
    dockerClient
        .copyArchiveToContainerCmd(containerId)
        .withHostResource(dockerComposeFile.getAbsolutePath())
        .withRemotePath("/")
        .exec();
  }

  private void removeContainer(String containerId){
    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
  }
  private DCResponse getContainerLogs(String containerId) throws InterruptedException {
    val callback = new DCLogger();
    try{
      dockerClient.logContainerCmd(containerId)
          .withStdOut(true)
          .withStdErr(true)
          .withFollowStream(true)
          .exec(callback)
          .awaitCompletion(1, TimeUnit.HOURS);

      /**
       * NOTE: for some reason, false stderrs are reported via the docker logs command for the docker-compose container.
       * It is not clear why the output of, `docker-compose kill`, shows up as STDERR when docker logs is run,
       * even though there were no errors. Because of this, we do manual error matching via the `ERROR:` keyword.
       * This is not ideal, but is better than what there was.
       */
      val actuallyHasError = callback.hasError() && stream(callback.getStderr().split("\n"))
          .anyMatch(x -> x.contains("ERROR:"));
      if (true){
        return DCResponse.builder()
            .stderr(callback.getStderr())
            .stdout(callback.getStdout())
            .build();
      } else {
        return DCResponse.builder()
            .stderr("")
            .stdout(callback.getStdout()+callback.getStderr())
            .build();
      }

    } catch (InterruptedException e) {
      log.error("ERROR[{}]: {}", e.getClass().getSimpleName(), e.getMessage());
      throw e;
    }
  }

  private void runContainer(String containerId) throws InterruptedException {
    dockerClient.startContainerCmd(containerId).exec();
    dockerClient.waitContainerCmd(containerId);
  }

  @Value
  @Builder
  public static class DCResponse{
    @NonNull private final String stdout;
    @NonNull private final String stderr;

    public boolean hasErrors(){
      return !isBlank(stderr);
    }
  }

  @Slf4j
  public static class DCLogger extends ResultCallbackTemplate<DCLogger, Frame>{

    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private boolean hasError = false;

    @Override
    public void onNext(Frame frame) {
      if (frame != null) {
        switch (frame.getStreamType()) {
        case STDOUT:
          stdout.append(new String(frame.getPayload()));
          break;
        case STDERR:
          stderr.append(new String(frame.getPayload()));
          hasError = true;
          break;
        default:
          log.error("unknown stream type:" + frame.getStreamType());
        }
      }
    }

    public String getStdout(){
      return stdout.toString();
    }

    public String getStderr(){
      return stderr.toString();
    }

    public boolean hasError(){
      return hasError;
    }

  }


}
