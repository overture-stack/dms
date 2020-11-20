package bio.overture.dms.infra.docker;

import bio.overture.dms.core.util.Nullable;
import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.docker.model.DockerImage;
import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.properties.service.ServiceProperties;
import bio.overture.dms.infra.util.FileUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static bio.overture.dms.core.util.Exceptions.checkArgument;
import static bio.overture.dms.core.util.Strings.isBlank;
import static bio.overture.dms.core.util.Concurrency.waitForFutures;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;
import static com.github.dockerjava.api.model.MountType.VOLUME;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@RequiredArgsConstructor
public class DockerService {

  private static final String MAINTENANCE_IMAGE_NAME = "alpine:3.12";

  @NonNull private final String dmsVolumeName;
  @NonNull private final DockerClient client;
  @NonNull private final EnvProcessor envProcessor;

  public String createRandomLocalVolume(){
    return client.createVolumeCmd().exec().getName();
  }

  public void createVolume(@NonNull String volumeName){
    client.listVolumesCmd().withFilter("name",List.of(volumeName))
        .exec().getVolumes().stream().findFirst()
        .ifPresentOrElse(x -> {
          log.info("The volume '{}' already exists, skipping creation", volumeName);
        }, () -> client.createVolumeCmd()
            .withName(volumeName)
            .exec());
  }

  public void ping() {
    client.pingCmd().exec();
  }

  private Optional<Network> findNetwork(String networkName) {
    return client.listNetworksCmd().exec().stream()
        .filter(x -> x.getName().equals(networkName))
        .findFirst();
  }


  private List<String> extractContainerEnv(Map<String, ?> envMap){
    return envMap .entrySet()
        .stream()
        .map(x -> x.getKey()+"="+x.getValue().toString())
        .collect(toUnmodifiableList());
  }

  private List<String> extractContainerEnv(ServiceProperties serviceProperties){
    return extractContainerEnv(envProcessor.generateEnvMap(serviceProperties));
  }

  private <C extends Collection<Integer>> List<ExposedPort> extractExposedPorts(C exposedPorts){
    return exposedPorts.stream()
        .map(ExposedPort::tcp)
        .collect(toUnmodifiableList());
  }
  private List<ExposedPort> extractExposedPorts(DockerContainer<?> dockerContainer){
    return extractExposedPorts(dockerContainer.getExposedPorts());
  }

  private List<Mount> extractMounts(String assetVolumeName, Map<String, String> volumes){
    return volumes.entrySet()
        .stream()
        .map(e -> new Mount().withType(MountType.BIND).withSource(e.getKey()).withTarget(e.getValue()))
        .collect(toUnmodifiableList());
  }

  @SneakyThrows
  public String createVolumeWithAssets(){
    val volumeName = createRandomLocalVolume();

    pullImage(MAINTENANCE_IMAGE_NAME);
    val volumeMountPath = "/dms";

    val containerId = client.createContainerCmd(MAINTENANCE_IMAGE_NAME)
        .withHostConfig(
            newHostConfig().withMounts(List.of(new Mount().withType(VOLUME).withSource(volumeName).withTarget(volumeMountPath))))
        .exec().getId();
    val rootAssetsPath= FileUtils.readResourcePath("/assets").getFile().toPath().toAbsolutePath().toString();
   client
        .copyArchiveToContainerCmd(containerId)
        .withHostResource(rootAssetsPath)
        .withRemotePath(volumeMountPath)
        .exec();

   client.removeContainerCmd(containerId).withForce(true).exec();
   return volumeName;
  }

  @SneakyThrows
  public static Path copyAssetsToDisk(){
    val tempDirPath = Files.createTempDirectory("assets");
    val rootAssetsPath= FileUtils.readResourcePath("/assets/").getFile().toPath();
    walk(rootAssetsPath).skip(1).forEach(path -> copyPath(path, rootAssetsPath, tempDirPath));
    return tempDirPath;
  }

  @SneakyThrows
  private static void copyPath(Path path, Path sourceDir, Path targetDir){
    log.info("file: {}",path.toString());
    val relativePath = sourceDir.relativize(path);
    val newPath = targetDir.resolve(relativePath);
    if (isDirectory(path)){
        createDirectories(newPath);
    } else if (isRegularFile(path) && isReadable(path)){
      copy(path, newPath);
    } else {
      throw new IllegalStateException("could not process assets");
    }
  }

  private List<Volume> extractVolumes(DockerContainer<?> dockerContainer){
    return dockerContainer.getVolumes().stream()
        .map(x -> Volume.parse(new HashMap<>()))
        .collect(toUnmodifiableList());
  }

  public String createContainer(@NonNull String networkName, @Nullable String assetVolumeName, @NonNull DCService s){
    checkArgument(s.getVolumes().isEmpty() || !isBlank(assetVolumeName),
          "Cannot create container since assetVolumeName is blank and mounts are expected");
    val createContainerCmd = client.createContainerCmd(s.getImage())
        .withName(s.getServiceName())
        .withAttachStderr(true)
        .withAttachStdout(true)
        .withAliases(s.getServiceName());

    val hostConfig = newHostConfig()
        .withNetworkMode(networkName);

    if (!s.getEnvironment().isEmpty()){
        createContainerCmd.withEnv(extractContainerEnv(s.getEnvironment()));
    }

    if (!s.getExpose().isEmpty()){
        createContainerCmd.withExposedPorts(extractExposedPorts(s.getExpose()));
    }

    if (!s.getPorts().isEmpty()){
      val portBindings = s.getPorts().entrySet()
          .stream()
          .map(e -> new PortBinding(Ports.Binding.bindPort(e.getKey()), ExposedPort.tcp(e.getValue())))
          .collect(toUnmodifiableList());
      hostConfig.withPortBindings(portBindings);
    }

    if (!s.getVolumes().isEmpty()){
      hostConfig.withMounts(extractMounts(assetVolumeName, s.getVolumes()));
    }

    createContainerCmd.withHostConfig(hostConfig);

    return createContainerCmd.exec().getId();
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
    val networkId = getOrCreateNetwork(dockerContainer.getNetwork());
    client.connectToNetworkCmd()
        .withContainerId(containerId)
        .withNetworkId(networkId)
        .exec();
    return containerId;
  }

  public void startContainer(String containerId){
    client.startContainerCmd(containerId)
        .exec();
  }

  public void deleteContainersByName(@NonNull ExecutorService executorService,  @NonNull Collection<String> containerNames,
      boolean force, boolean removeVolumes){
    val futures = containerNames.stream()
        .map(name -> executorService.submit(() -> {
          log.info("Starting deletion of container '{}'", name);
          deleteContainerByName(name, force, removeVolumes);
          log.info("Finished deleting container '{}'", name);
        }))
        .collect(toUnmodifiableList());
    waitForFutures(futures);
  }

  public void deleteContainerByName(@NonNull String containerName, boolean force, boolean removeVolumes){
    findContainerId(containerName).ifPresent(id -> deleteContainer(id, force, removeVolumes));
  }

  public void deleteContainer(@NonNull String containerId, boolean force, boolean removeVolumes){
    client.removeContainerCmd(containerId)
        .withForce(force)
        .withRemoveVolumes(removeVolumes)
        .exec();
  }
  public String getOrCreateNetwork(String networkName) {
    return findNetwork(networkName)
        .map(Network::getId)
        .orElseGet(() -> client.createNetworkCmd().withName(networkName).exec().getId());
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

  //TODO: first do concurrent pulling of images. When EGO pulling is done, then continue with EGO deployement
  @SneakyThrows
  public void pullImage(@NonNull DockerImage dockerImage) {
    client.pullImageCmd(dockerImage.getName()).withTag(dockerImage.getTag())
        .start()
        .awaitCompletion();
  }

  @SneakyThrows
  public void pullImage(@NonNull String imageName) {
    client.pullImageCmd(imageName)
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
  public Optional<InspectContainerResponse> inspectContainerByName(@NonNull String containerName){
    return findContainerId(containerName)
        .map(id -> client.inspectContainerCmd(id).exec());
  }
  public Optional<ContainerState> findContainerState(@NonNull String containerId){
    try {
      return Optional.of(client.inspectContainerCmd(containerId).exec().getState());
    } catch (com.github.dockerjava.api.exception.NotFoundException e){
      return Optional.empty();
    }
  }

  private Stream<String> streamContainerNames(Container c){
    return Arrays.stream(c.getNames())
        // This transformation is needed, since the names array prefixes all container name with '/'
        .map(x -> x.substring(1));
  }

  private boolean isContainerNameMatch(Container c, String name){
    return streamContainerNames(c)
        .anyMatch(x -> x.equals(name));
  }

  public Optional<String> findContainerId(@NonNull String name){
    return client.listContainersCmd()
        .withNameFilter(List.of(name))
        .exec()
        .stream()
        .filter(x -> isContainerNameMatch(x, name))
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
