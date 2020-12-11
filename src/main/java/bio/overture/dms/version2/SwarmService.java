package bio.overture.dms.version2;

import static bio.overture.dms.version2.DeploymentStates.INFLIGHT;
import static bio.overture.dms.version2.DeploymentStates.SUCCESSFUL;
import static bio.overture.dms.version2.DeploymentStates.UNSUCCESSFUL;
import static com.github.dockerjava.api.model.MountType.VOLUME;
import static com.github.dockerjava.api.model.TaskState.COMPLETE;
import static com.github.dockerjava.api.model.TaskState.RUNNING;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.dms.util.SafeGet;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.RemoveVolumeCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.SwarmNode;
import com.github.dockerjava.api.model.SwarmNodeManagerStatus;
import com.github.dockerjava.api.model.Task;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.TaskStatus;
import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwarmService {

  private static final List<Task> EMPTY_TASKS = List.of();

  private final DockerClient client;
  private final SwarmSpecService swarmSpecService;

  @Autowired
  public SwarmService(@NonNull DockerClient client, @NonNull SwarmSpecService swarmSpecService) {
    this.client = client;
    this.swarmSpecService = swarmSpecService;
  }

  public void ping() {
    client.pingCmd().exec();
  }

  // TODO: implement this. List the tasks, filter on running, and should return N tasks, where N is
  // also the number of replicas.
  public List<String> getRunningContainersForService(@NonNull String serviceName) {
    return null;
  }

  public void deleteServices(@NonNull List<String> names, boolean destroyVolumes) {
    streamSwarmServices(names)
        .forEach(
            s -> {
              // TODO: BUG there is a bug here. volume removal happens right after remove service
              // which is async.
              // Need to wait for service to be killed and removed before volume can be removed.
              // Best to do this concurrently
              client.removeServiceCmd(s.getId()).exec();
              if (destroyVolumes) {
                streamVolumeNames(s).map(client::removeVolumeCmd).forEach(RemoveVolumeCmd::exec);
              }
            });
  }

  private Stream<String> streamVolumeNames(Service s) {
    return SafeGet.of(s, Service::getSpec)
        .map(ServiceSpec::getTaskTemplate)
        .map(TaskSpec::getContainerSpec)
        .map(ContainerSpec::getMounts)
        .find()
        .map(SwarmService::streamVolumeMountSources)
        .orElseGet(
            () -> {
              log.error(
                  "Could not extract ServiceSpec->TaskTemplate->ContainerSpec->Mounts from serviceId '{}' as one of them was null",
                  s.getId());
              return Stream.of();
            });
  }

  public Optional<String> findServiceName(@NonNull String name) {
    return findServiceNames(List.of(name)).stream().findFirst();
  }

  public Set<String> findServiceNames(@NonNull List<String> names) {
    return streamSwarmServices(names)
        .map(Service::getSpec)
        .filter(x -> nonNull(x.getName()))
        .map(ServiceSpec::getName)
        .collect(toUnmodifiableSet());
  }

  public Stream<Service> streamSwarmServices(@NonNull List<String> names) {
    val set = new HashSet<>(names);
    return client.listServicesCmd().withNameFilter(names).exec().stream()
        .filter(x -> nonNull(x.getSpec()) && nonNull(x.getSpec().getName()))
        .filter(x -> set.contains(x.getSpec().getName()));
  }

  public Optional<SwarmServiceInfo> findSwarmServiceInfo(
      @NonNull String serviceName, boolean includeTasks) {
    return findServiceName(serviceName)
        .map(serviceId -> buildSwarmServiceInfo(serviceName, serviceId, includeTasks));
  }

  public void updateSwarmService(String serviceId, ServiceSpec s, Long version) {
    client.updateServiceCmd(serviceId, s).withVersion(version).exec();
  }

  //  private void sdf(String serviceId){
  //    client.listTasksCmd().exec().
  //
  //  }
  //  public boolean isSwarmServiceRunning(@NonNull String name){
  //    findSwarmService(name)
  //        .map(x -> {
  //
  //
  //
  //
  //        })
  //
  //  }

  @SneakyThrows
  public void pullImage(@NonNull String imageName) {
    client.pullImageCmd(imageName).start().awaitCompletion();
  }

  public void createVolume(@NonNull String volumeName) {
    client.listVolumesCmd().withFilter("name", List.of(volumeName)).exec().getVolumes().stream()
        .findFirst()
        .ifPresentOrElse(
            x -> {
              log.info("The volume '{}' already exists, skipping creation", volumeName);
            },
            () -> client.createVolumeCmd().withName(volumeName).exec());
  }

  public String getOrCreateNetwork(String networkName) {
    return findNetwork(networkName)
        .map(Network::getId)
        .orElseGet(
            () ->
                client
                    .createNetworkCmd()
                    .withName(networkName)
                    .withAttachable(true)
                    .withDriver("overlay")
                    .exec()
                    .getId());
  }

  // TODO: there should be 2 modes of deployment. A) is probably the best
  // a) Blocking the deployment of a dependency until its parents is READY
  // b) Not blocking any dependency, and deploy all. Once deployed, run a separate command to poll
  // and check status.
  //    If something didnt start or is not ready, it should report it
  @SneakyThrows
  public void initializeSwarm() {
    val swarmSpec = swarmSpecService.getInitSwarmSpec();
    boolean create = false;
    try {
      val results = client.listSwarmNodesCmd().exec();
      if (results.isEmpty()) {
        client.initializeSwarmCmd(swarmSpec).withForceNewCluster(true).exec();
      } else {
        create =
            results.stream()
                .map(SwarmNode::getManagerStatus)
                .filter(x -> !isNull(x))
                .noneMatch(SwarmNodeManagerStatus::isLeader);
      }
    } catch (DockerException t) {
      if (t.getHttpStatus() == 503) {
        create = true;
      }
    }
    if (create) {
      client.initializeSwarmCmd(swarmSpec).withForceNewCluster(false).exec();
    }
  }

  public DeploymentStates readServiceState(String serviceName) {
    val service =
        client.listServicesCmd().withNameFilter(List.of(serviceName)).exec().stream()
            .findFirst()
            .get();
    long maxAttempts = service.getSpec().getTaskTemplate().getRestartPolicy().getMaxAttempts();
    return internalReadServiceState(serviceName, maxAttempts);
  }

  @SneakyThrows
  public DeploymentStates waitForServiceRunning(
      String serviceName, Duration timeout, Duration poll) {
    val service =
        client.listServicesCmd().withNameFilter(List.of(serviceName)).exec().stream()
            .findFirst()
            .get();

    long maxAttempts = service.getSpec().getTaskTemplate().getRestartPolicy().getMaxAttempts();

    val sw = Stopwatch.createStarted();
    while (true) {
      val resolvedState = internalReadServiceState(serviceName, maxAttempts);
      if (resolvedState == INFLIGHT) {
        log.info("Service {} is not ready yet, waiting", serviceName);
      } else if (resolvedState == UNSUCCESSFUL) {
        log.error(
            "Service {} was UNSUCCESSFUL in deploying properly, even after {} retries",
            serviceName,
            maxAttempts);
        return resolvedState;
      } else if (resolvedState == SUCCESSFUL) {
        log.info("Service {} was SUCCESSFUL in deploying properly", serviceName);
        return resolvedState;
      }

      val currTime = sw.elapsed(TimeUnit.NANOSECONDS);
      if (currTime >= timeout.toNanos()) {
        sw.stop();
        log.error(
            "Service {} was UNSUCCESSFUL in deploying properly, as it timed out", serviceName);
        return UNSUCCESSFUL;
      }
      Thread.sleep(poll.toMillis());
    }
  }

  public String createSwarmService(@NonNull ServiceSpec serverSpec) {
    return client.createServiceCmd(serverSpec).exec().getId();
  }

  private SwarmServiceInfo buildSwarmServiceInfo(
      String serviceName, String serviceId, boolean includeTasks) {
    val version = client.inspectServiceCmd(serviceId).exec().getVersion().getIndex();
    val b =
        SwarmServiceInfo.builder()
            .tasks(EMPTY_TASKS)
            .name(serviceName)
            .id(serviceId)
            .version(version);
    if (includeTasks) {
      val tasks =
          client.listTasksCmd().withServiceFilter(serviceName).exec().stream()
              .filter(x -> x.getServiceId().equals(serviceId))
              .collect(toUnmodifiableList());
      b.tasks(tasks);
    }
    return b.build();
  }

  private DeploymentStates internalReadServiceState(String serviceName, long maxAttempts) {
    val allTasks =
        client.listTasksCmd().withServiceFilter(serviceName).exec().stream()
            .sorted(comparing(Task::getUpdatedAt).reversed())
            .collect(toUnmodifiableList());

    val numTasksNotYetRunning =
        allTasks.stream()
            .map(Task::getStatus)
            .map(TaskStatus::getState)
            .filter(state -> state.ordinal() < RUNNING.ordinal())
            .count();

    val numTasksRunning =
        allTasks.stream()
            .map(Task::getStatus)
            .map(TaskStatus::getState)
            .filter(state -> state == RUNNING || state == COMPLETE)
            .count();

    return resolveDeploymentState(
        numTasksNotYetRunning, numTasksRunning, allTasks.size(), maxAttempts);
  }

  private Optional<Network> findNetwork(String networkName) {
    return client.listNetworksCmd().exec().stream()
        .filter(x -> x.getName().equals(networkName))
        .filter(x -> x.getDriver().equals("overlay"))
        .findFirst();
  }

  private static Stream<String> streamVolumeMountSources(List<Mount> mounts) {
    return mounts.stream().filter(x -> x.getType() == VOLUME).map(Mount::getSource);
  }

  public static DeploymentStates resolveDeploymentState(
      long numTasksNotYetRunning, long numTasksRunning, long numTasksTotal, long maxAttempts) {

    if (numTasksRunning > 0 && numTasksNotYetRunning == 0) {
      log.info(
          "Found {} running or completed tasks and 0 not run tasks, stopping wait",
          numTasksNotYetRunning);
      // nothing is pending to be run, and there are some running current running, this means the
      // deployment was a success
      return SUCCESSFUL;
    } else if (numTasksTotal < maxAttempts) {
      // Scenario 1: Just started probably. need to  give some time for tasks to be scheduled.
      // Scenario 2: nothing in a running state yet, but they have been started/scheduled
      // Scenario 3: Some tasks are in a running state and there are others that have been scheduled
      // but not reached a decidable state yet
      return INFLIGHT;
    } else {
      // exhausted all retries, try and conclude
      if (numTasksNotYetRunning > 0) {
        // there are some things pending to be run, so we need to wait for them to either be in the
        // running state or fail state
        return INFLIGHT;
      } else if (numTasksRunning == 0) {
        // nothing is pending to be run, and there are no current running, and no new tasks will be
        // created and this means the deployement failed
        return UNSUCCESSFUL;
      }
      return INFLIGHT;
    }
  }

  @Value
  @Builder
  public static class SwarmServiceInfo {
    @NonNull private final String name;
    @NonNull private final String id;
    private final Long version;
    @NonNull private final List<Task> tasks;
  }
}
