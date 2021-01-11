package bio.overture.dms.docker;

import static bio.overture.dms.core.util.Concurrency.waitForFutures;
import static bio.overture.dms.core.util.FileUtils.readResourcePath;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;
import static com.github.dockerjava.api.model.MountType.VOLUME;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.core.util.Nullable;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/** Various convenience methods around managing non-swarm docker resources */
@Slf4j
@RequiredArgsConstructor
// Deprecated since containers are now managed by Swarm. There are still useful methods here,
// that have had some investement of time to get working.
// These methods should be removed if not used by version 1.0.0
@Deprecated(forRemoval = true)
public class DockerService {

  private static final String MAINTENANCE_IMAGE_NAME = "alpine:3.12";

  @NonNull private final DockerClient client;

  public void ping() {
    client.pingCmd().exec();
  }

  /** Volume Management */
  public String provisionRandomLocalVolume() {
    return client.createVolumeCmd().exec().getName();
  }

  public void provisionVolume(@NonNull String volumeName) {
    client.listVolumesCmd().withFilter("name", List.of(volumeName)).exec().getVolumes().stream()
        .findFirst()
        .ifPresentOrElse(
            x -> log.info("The volume '{}' already exists, skipping creation", volumeName),
            () -> client.createVolumeCmd().withName(volumeName).exec());
  }

  @SneakyThrows
  public String createVolumeWithAssets() {
    val volumeName = provisionRandomLocalVolume();

    pullImage(MAINTENANCE_IMAGE_NAME);
    val volumeMountPath = "/dms";

    val containerId =
        client
            .createContainerCmd(MAINTENANCE_IMAGE_NAME)
            .withHostConfig(
                newHostConfig()
                    .withMounts(
                        List.of(
                            new Mount()
                                .withType(VOLUME)
                                .withSource(volumeName)
                                .withTarget(volumeMountPath))))
            .exec()
            .getId();
    val rootAssetsPath = readResourcePath("/assets").getFile().toPath().toAbsolutePath().toString();
    client
        .copyArchiveToContainerCmd(containerId)
        .withHostResource(rootAssetsPath)
        .withRemotePath(volumeMountPath)
        .exec();

    client.removeContainerCmd(containerId).withForce(true).exec();
    return volumeName;
  }

  /** Network Management */
  public String getOrCreateNetwork(String networkName) {
    return findNetwork(networkName)
        .map(Network::getId)
        .orElseGet(() -> client.createNetworkCmd().withName(networkName).exec().getId());
  }

  /** Container Search and Inspection Methods */
  public Optional<String> findContainerId(@NonNull String name) {
    return client.listContainersCmd().withNameFilter(List.of(name)).exec().stream()
        .filter(x -> isContainerNameMatch(x, name))
        .map(Container::getId)
        .findFirst();
  }

  public Boolean isContainerRunning(String containerId) {
    val result = findContainerState(containerId);
    if (result.isEmpty()) {
      return false;
    }
    return result.get().getRunning();
  }

  public Optional<InspectContainerResponse> inspectContainerByName(@NonNull String containerName) {
    return findContainerId(containerName).map(id -> client.inspectContainerCmd(id).exec());
  }

  public Optional<ContainerState> findContainerState(@NonNull String containerId) {
    try {
      return Optional.of(client.inspectContainerCmd(containerId).exec().getState());
    } catch (com.github.dockerjava.api.exception.NotFoundException e) {
      return Optional.empty();
    }
  }

  /** Container Management Methods */
  public void deleteContainersByName(
      @NonNull ExecutorService executorService,
      @NonNull Collection<String> containerNames,
      boolean force,
      boolean removeVolumes) {
    val futures =
        containerNames.stream()
            .map(
                name ->
                    executorService.submit(
                        () -> {
                          log.info("Starting deletion of container '{}'", name);
                          deleteContainerByName(name, force, removeVolumes);
                          log.info("Finished deleting container '{}'", name);
                        }))
            .collect(toUnmodifiableList());
    waitForFutures(futures);
  }

  public void deleteContainerByName(
      @NonNull String containerName, boolean force, boolean removeVolumes) {
    findContainerId(containerName).ifPresent(id -> deleteContainer(id, force, removeVolumes));
  }

  public void deleteContainer(@NonNull String containerId, boolean force, boolean removeVolumes) {
    client.removeContainerCmd(containerId).withForce(force).withRemoveVolumes(removeVolumes).exec();
  }

  public DockerExecResponse exec(
      @NonNull String containerId, @NonNull String command, @Nullable InputStream stdin) {
    val useStdin = !isNull(stdin);
    val execId =
        client
            .execCreateCmd(containerId)
            .withTty(true)
            .withAttachStderr(true)
            .withAttachStdout(true)
            .withAttachStdin(useStdin)
            .withCmd(command)
            .exec()
            .getId();
    val execCmd = client.execStartCmd(execId);
    if (useStdin) {
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
  public void pullImage(@NonNull String imageName) {
    client.pullImageCmd(imageName).start().awaitCompletion();
  }

  private Stream<String> streamContainerNames(Container c) {
    return Arrays.stream(c.getNames())
        // This transformation is needed, since the names array prefixes all container name with '/'
        .map(x -> x.substring(1));
  }

  private boolean isContainerNameMatch(Container c, String name) {
    return streamContainerNames(c).anyMatch(x -> x.equals(name));
  }

  private Optional<Network> findNetwork(String networkName) {
    return client.listNetworksCmd().exec().stream()
        .filter(x -> x.getName().equals(networkName))
        .findFirst();
  }

  private <C extends Collection<Integer>> List<ExposedPort> extractExposedPorts(C exposedPorts) {
    return exposedPorts.stream().map(ExposedPort::tcp).collect(toUnmodifiableList());
  }

  private List<Mount> extractMounts(Map<String, String> volumes) {
    return volumes.entrySet().stream()
        .map(
            e ->
                new Mount()
                    .withType(MountType.BIND)
                    .withSource(e.getKey())
                    .withTarget(e.getValue()))
        .collect(toUnmodifiableList());
  }

  @Value
  @Builder
  @Deprecated(forRemoval = true)
  public static class DockerExecResponse {
    @NonNull private final OutputStream stdout;
    @NonNull private final OutputStream stderr;
  }
}
