package bio.overture.dms.infra.docker;

import bio.overture.dms.core.Splitter;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.util.SafeGet;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.Mount;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static bio.overture.dms.core.Joiner.EQUALS;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

@RequiredArgsConstructor
public class DCServiceStateReader {

  @NonNull private final DockerService dockerService;


  public Optional<DCService> readServiceState(@NonNull String serviceName){
    return dockerService.inspectContainerByName(serviceName)
        .map(x -> doit(x, serviceName));
  }

  private DCService doit(@NonNull InspectContainerResponse r, @NonNull String serviceName){
    val out = new DCService();
    extractEnvs(r).ifPresent(out::setEnvironment);
    out.setExpose(extractExposedPorts(r));
    out.setImage(extractImageName(r));
    extractPorts(r).ifPresent(out::setPorts);
    out.setServiceName(serviceName);
    extractMounts(r).ifPresent(out::setVolumes);
    return out;
  }

  private String extractImageName(InspectContainerResponse r){
    return r.getConfig().getImage();
  }

  private Optional<Map<String, String>> extractMounts(InspectContainerResponse r){
    return SafeGet.of(r, InspectContainerResponse::getMounts)
        .map(mounts -> mounts.stream()
            .collect(toUnmodifiableMap(Mount::getSource,
                x -> x.getDestination().getPath())))
        .get();
  }

  private Set<Integer> extractExposedPorts(InspectContainerResponse r){
    return stream(r.getConfig().getExposedPorts())
        .map(ExposedPort::getPort)
        .collect(toUnmodifiableSet());
  }

  private Optional<String> extractNetworkName(InspectContainerResponse r){
    return SafeGet.of(r, InspectContainerResponse::getHostConfig)
        .map(HostConfig::getNetworkMode)
        .get();
  }

  private Optional<Map<Integer,Integer>> extractPorts(InspectContainerResponse r){
    return SafeGet.of(r, InspectContainerResponse::getHostConfig)
        .map(HostConfig::getPortBindings)
        .map(Ports::getBindings)
        .map(bindings -> {
          val map = new HashMap<Integer, Integer>();
          bindings.forEach((key, value) -> {
            val containerPort = extractContainerPort(key);
            val hostPortBindings = extractHostPorts(value);
            hostPortBindings.forEach(hostPort -> map.put(hostPort, containerPort));
          });
          return Map.copyOf(map);
        })
        .get();
  }

  private Integer extractContainerPort(ExposedPort p){
    return p.getPort();
  }

  private Set<Integer> extractHostPorts(Ports.Binding[] bindings){
    return stream(bindings)
        .map(Ports.Binding::getHostPortSpec)
        .map(Integer::parseInt)
        .collect(toUnmodifiableSet());
  }

  private Optional<Map<String, String>> extractEnvs(InspectContainerResponse r){
    return SafeGet.of(r, InspectContainerResponse::getConfig)
        .map(ContainerConfig::getEnv)
        .map(a -> stream(a)
            .map(x -> Splitter.EQUALS.split(x, true))
            .collect(toUnmodifiableMap(x -> x.get(0),
                x -> EQUALS.join(x.stream().skip(1)))))
        .get();
  }
}
