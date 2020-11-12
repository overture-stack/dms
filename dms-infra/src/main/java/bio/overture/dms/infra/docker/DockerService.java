package bio.overture.dms.infra.docker;

import bio.overture.dms.core.Nullable;
import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.docker.model.DockerImage;
import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.properties.service.ServiceProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bio.overture.dms.infra.docker.NotFoundException.buildNotFoundException;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;

@RequiredArgsConstructor
public class DockerService {

  @NonNull private final DockerClient client;
  @NonNull private final EnvProcessor envProcessor;


  public void ping() {
    client.pingCmd().exec();
  }

  private Optional<Network> findNetwork(String networkName) {
    return client.listNetworksCmd().exec().stream()
        .filter(x -> x.getName().equals(networkName))
        .findFirst();
  }


  private List<String> extractContainerEnv(ServiceProperties serviceProperties){
    return envProcessor.generateEnvMap(serviceProperties)
        .entrySet()
        .stream()
        .map(x -> x.getKey()+"="+x.getValue())
        .collect(toUnmodifiableList());
  }

  private List<ExposedPort> extractExposedPorts(DockerContainer<?> dockerContainer){
    return dockerContainer.getExposedPorts().stream()
        .map(ExposedPort::tcp)
        .collect(toUnmodifiableList());
  }

  private List<Volume> extractVolumes(DockerContainer<?> dockerContainer){
    return dockerContainer.getVolumes().stream()
        .map(x -> Volume.parse(new HashMap<>()))
        .collect(toUnmodifiableList());
  }


  public String createContainer(@NonNull DockerContainer<?> dockerContainer){
    val containerId = client.createContainerCmd(dockerContainer.getDockerImage().getFullName())
        .withName(dockerContainer.getName())
        .withAttachStderr(true)
        .withAttachStdout(true)
        .withEnv(extractContainerEnv(dockerContainer.getServiceProperties()))
        .withExposedPorts(extractExposedPorts(dockerContainer))
        .withVolumes(extractVolumes(dockerContainer))
        .exec()
        .getId();
    val n = getNetwork(dockerContainer.getNetwork());
    client.connectToNetworkCmd()
        .withContainerId(containerId)
        .withNetworkId(n.getId())
        .exec();
    return containerId;
  }

  public void startContainer(String containerId){
    client.startContainerCmd(containerId)
        .exec();
  }

  public void deleteContainer(@NonNull String containerId, boolean force, boolean removeVolumes){
    client.removeContainerCmd(containerId)
        .withForce(force)
        .withRemoveVolumes(removeVolumes)
        .exec();
  }
  public Network getNetwork(String networkName) {
    return findNetwork(networkName)
        .orElseGet(
            () -> {
              client.createNetworkCmd().withName(networkName).exec();
              return findNetwork(networkName)
                  .orElseThrow(
                      () ->
                          new IllegalStateException(
                              format("could not create network \"%s\"", networkName)));
            });
  }

  @Value
  @Builder
  public static class DockerExecResponse{
    @NonNull private final OutputStream stdout;
    @NonNull private final OutputStream stderr;
  }

  public DockerExecResponse exec(@NonNull String containerId, @NonNull String command, @Nullable InputStream stdin){
    val useStdin = !isNull(stdin) ;
    val execId = client.execCreateCmd(containerId)
        .withTty(true)
        .withAttachStderr(true)
        .withAttachStdout(true)
        .withAttachStdin(useStdin)
        .withCmd(command).exec().getId();
    val execCmd = client.execStartCmd(execId);
    if (useStdin){
      execCmd.withStdIn(stdin);
    }

    val stdoutOutputStream = new ByteArrayOutputStream();
    val stderrOutputStream = new ByteArrayOutputStream();
    execCmd.exec(new ExecStartResultCallback(stdoutOutputStream, stderrOutputStream));
    return DockerExecResponse.builder()
        .stderr(stderrOutputStream)
        .stdout(stdoutOutputStream)
        .build();
  }

  @SneakyThrows
  public void pullImage(@NonNull DockerImage dockerImage) {
    client.pullImageCmd(dockerImage.getName()).withTag(dockerImage.getTag())
        .start()
        .awaitCompletion();
  }

  @Value
  @Builder
  public static class DockerContainerInfo{
    @NonNull private final String id;
    @NonNull private final ContainerState state;
  }

  public Boolean isContainerRunning(String containerId){
    val result = findContainerState(containerId);
    if (result.isEmpty()){
      return false;
    }
    return result.get().getRunning();

  }
  public Optional<ContainerState> findContainerState(@NonNull String containerId){
    try {
      return Optional.of(client.inspectContainerCmd(containerId).exec().getState());
    } catch (com.github.dockerjava.api.exception.NotFoundException e){
      return Optional.empty();
    }
  }

  public Optional<String> findContainerId(@NonNull String name){
    return client.listContainersCmd()
        .withNameFilter(List.of(name))
        .withLimit(1)
        .exec()
        .stream()
        .map(Container::getId)
        .findFirst();
  }


  private Optional<Image> findImage(String repo, String tag) {
    return client.listImagesCmd().exec().stream()
        .filter(x -> matchTag(x.getRepoTags(), repo, tag))
        .findFirst();
  }

  private Image readImage(String repo, String tag) {
    return findImage(repo, tag)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    format("The repo \"%s\" with tag \"%s\" does not exist", repo, tag)));
  }

  public void deleteImage(String repo, String tag) {
    val image = readImage(repo, tag);
    client.removeImageCmd(image.getId()).exec();
  }

  public static String resolveRepoTag(String repo, String tag) {
    return repo + ":" + tag;
  }

  private static boolean matchTag(String[] repoTags, String inputRepo, String inputTag) {
    return stream(repoTags)
        .anyMatch(actualRepoTag -> actualRepoTag.equals(resolveRepoTag(inputRepo, inputTag)));
  }
}
