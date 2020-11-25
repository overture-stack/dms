package bio.overture.dms.compose;

import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;
import static bio.overture.dms.util.CollectionUtils.mapToUnmodifiableSet;
import static bio.overture.dms.util.Joiner.EQUALS;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.dms.docker.DockerService;
import bio.overture.dms.model.compose.Compose;
import bio.overture.dms.model.compose.ComposeService;
import bio.overture.dms.model.compose.ComposeServiceInfo;
import bio.overture.dms.util.SafeGet;
import bio.overture.dms.util.Splitter;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class ComposeManager {

  @NonNull private final ExecutorService executorService;
  @NonNull private final ComposeGraphGenerator graphGenerator;
  @NonNull private final DockerService dockerService;

  @SneakyThrows
  public void deploy(@NonNull Compose dc) {
    val graph = graphGenerator.generateGraph(dc);
    createConcurrentGraphTraversal(executorService, graph)
        .traverse(x -> x.getData().run(), () -> {});
  }

  @SneakyThrows
  public void destroy(@NonNull Compose dc, boolean force, boolean removeVolumes) {
    dockerService.deleteContainersByName(
        executorService,
        mapToUnmodifiableSet(dc.getServices(), ComposeService::getServiceName),
        force,
        removeVolumes);
  }

  /**
   * For a container name, resolve what is currently deployed with the best effort
   *
   * @param containerName The name of the container to inspect
   * @return Optional deploy info
   */
  public Optional<ComposeServiceInfo> readDeployInfo(@NonNull String containerName) {
    return dockerService.inspectContainerByName(containerName).map(x -> doit(x, containerName));
  }

  private ComposeServiceInfo doit(
      @NonNull InspectContainerResponse r, @NonNull String containerName) {
    val out = ComposeServiceInfo.builder();
    extractEnvs(r).ifPresent(out::environment);
    out.expose(extractExposedPorts(r));
    out.image(extractImageName(r));
    extractPorts(r).ifPresent(out::ports);
    out.containerName(containerName);
    extractMounts(r).ifPresent(out::mounts);
    return out.build();
  }

  private String extractImageName(InspectContainerResponse r) {
    return r.getConfig().getImage();
  }

  private Optional<Map<String, String>> extractMounts(InspectContainerResponse r) {
    return SafeGet.of(r, InspectContainerResponse::getMounts)
        .map(
            mounts ->
                mounts.stream()
                    .collect(
                        toUnmodifiableMap(
                            InspectContainerResponse.Mount::getSource,
                            x -> x.getDestination().getPath())))
        .get();
  }

  private Set<Integer> extractExposedPorts(InspectContainerResponse r) {
    return stream(r.getConfig().getExposedPorts())
        .map(ExposedPort::getPort)
        .collect(toUnmodifiableSet());
  }

  private Optional<String> extractNetworkName(InspectContainerResponse r) {
    return SafeGet.of(r, InspectContainerResponse::getHostConfig)
        .map(HostConfig::getNetworkMode)
        .get();
  }

  private Optional<Map<Integer, Integer>> extractPorts(InspectContainerResponse r) {
    return SafeGet.of(r, InspectContainerResponse::getHostConfig)
        .map(HostConfig::getPortBindings)
        .map(Ports::getBindings)
        .map(
            bindings -> {
              val map = new HashMap<Integer, Integer>();
              bindings.forEach(
                  (key, value) -> {
                    val containerPort = extractContainerPort(key);
                    val hostPortBindings = extractHostPorts(value);
                    hostPortBindings.forEach(hostPort -> map.put(hostPort, containerPort));
                  });
              return Map.copyOf(map);
            })
        .get();
  }

  private Integer extractContainerPort(ExposedPort p) {
    return p.getPort();
  }

  private Set<Integer> extractHostPorts(Ports.Binding[] bindings) {
    return stream(bindings)
        .map(Ports.Binding::getHostPortSpec)
        .map(Integer::parseInt)
        .collect(toUnmodifiableSet());
  }

  private Optional<Map<String, String>> extractEnvs(InspectContainerResponse r) {
    return SafeGet.of(r, InspectContainerResponse::getConfig)
        .map(ContainerConfig::getEnv)
        .map(
            a ->
                stream(a)
                    .map(x -> Splitter.EQUALS.split(x, true))
                    .collect(
                        toUnmodifiableMap(x -> x.get(0), x -> EQUALS.join(x.stream().skip(1)))))
        .get();
  }
}
