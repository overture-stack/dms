package bio.overture.dms.swarm.service;

import static bio.overture.dms.core.util.Concurrency.poll;
import static bio.overture.dms.core.util.Concurrency.waitForFutures;
import static bio.overture.dms.swarm.model.DeploymentStates.INFLIGHT;
import static bio.overture.dms.swarm.model.DeploymentStates.SUCCESSFUL;
import static bio.overture.dms.swarm.model.DeploymentStates.UNSUCCESSFUL;
import static com.github.dockerjava.api.model.MountType.VOLUME;
import static com.github.dockerjava.api.model.TaskState.COMPLETE;
import static com.github.dockerjava.api.model.TaskState.RUNNING;
import static java.time.Duration.ofMillis;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.core.util.SafeGet;
import bio.overture.dms.swarm.model.DeploymentStates;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwarmService {

  /** Constants */
  private static final List<Task> EMPTY_TASKS = List.of();

  /** Dependencies */
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

  /**
   * Concurrently deletes services, along with any containers/tasks associated with them. Can
   * optionally also delete volumes. Note: When a service is deleted, it does not mean the
   * containers/tasks associated with the service was deleted as well. Before volume deletion, we
   * need to ensure that both the services and their associated containers/tasks were deleted, so
   * that we can delete volumes with out errors. This is done by concurrently issuing a deletion
   * request for each service and then waiting (via a GuardedBlock and polling) for the associated
   * containers/tasks to be removed.
   *
   * @param names of the services to be removed
   * @param destroyVolumes If true, will destroy all volumes associated with the services
   */
  @SneakyThrows
  public void deleteServices(@NonNull Collection<String> names, boolean destroyVolumes) {
    // Get the volume names associated with the services
    val volumeNames =
        streamSwarmServices(names).flatMap(this::streamVolumeNames).collect(toUnmodifiableSet());

    // Create an index mapping a ServiceName to a list of containerIds
    val idx = getServiceContainerIndex(names);

    if (!idx.isEmpty()) {
      // Concurrently delete services and wait for the associated containers/tasks to be deleted as
      // well with the specified retry scheme
      concurrentlyDeletedServicesAndWait(idx, 133, ofMillis(1000));
    }

    if (destroyVolumes) {
      // cleanup dangling containers that are connected to the volumes, but that might not have been
      // associated with the previously running services.
      deleteContainersByVolumes(volumeNames);

      // With all dependencies removed (services and containers), finally delete the volume
      deleteVolumes(volumeNames);
    }
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
              log.debug(
                  "Could not extract ServiceSpec->TaskTemplate->ContainerSpec->Mounts "
                      + "from serviceName '{}' as one of them was null",
                  s.getSpec().getName());
              return Stream.of();
            });
  }

  private Map<String, Collection<String>> getServiceContainerIndex(
      Collection<String> serviceNames) {
    return streamSwarmServices(serviceNames)
        .map(Service::getSpec)
        .filter(Objects::nonNull)
        .map(ServiceSpec::getName)
        .collect(toUnmodifiableMap(identity(), this::getContainerIdsForService));
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private void concurrentlyDeletedServicesAndWait(
      Map<String, Collection<String>> idx, int numRetries, Duration poll) {
    val executors = newFixedThreadPool(idx.keySet().size());
    val futures =
        idx.entrySet().stream()
            .map(
                e -> {
                  val serviceName = e.getKey();
                  val containerIds = e.getValue();
                  return executors.submit(
                      () -> {
                        deleteServiceAndWait(serviceName, containerIds, numRetries, poll);
                      });
                })
            .collect(toUnmodifiableList());
    waitForFutures(futures);
    executors.shutdown();
    // Timeout to 1.5 times the expected retry period
    executors.awaitTermination((numRetries + (numRetries / 2)) * poll.toMillis(), MILLISECONDS);
  }

  private void deleteServiceAndWait(
      String serviceName, Collection<String> containerIds, int numRetries, Duration poll) {
    client.removeServiceCmd(serviceName).exec();
    // We want several threads to be able to call this stateless wait method
    // concurrently,
    // and so we create a unique monitor per thread. For more info, refer to
    // Guarded Blocks
    // https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html
    // ego-ui takes a long time to stop the container (the service stops quickly)
    if (!serviceName.equalsIgnoreCase("ego-ui")) {
      val lock = new Object();
      waitForContainerDeletion(lock, containerIds, numRetries, poll);
    }
  }

  private void deleteContainersByVolumes(Collection<String> volumeNames) {
    client.listContainersCmd().withShowAll(true).withVolumeFilter(volumeNames).exec().stream()
        .map(Container::getId)
        .map(client::removeContainerCmd)
        .forEach(RemoveContainerCmd::exec);
  }

  private void deleteVolumes(Set<String> volumeNames) {
    filterExistingVolumes(volumeNames).stream()
        .map(client::removeVolumeCmd)
        .map(x -> runAsync(x::exec))
        .forEach(CompletableFuture::join);
  }

  private Set<String> filterExistingVolumes(Set<String> volumeNames) {
    return client.listVolumesCmd().exec().getVolumes().stream()
        .map(InspectVolumeResponse::getName)
        .filter(volumeNames::contains)
        .collect(toUnmodifiableSet());
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

  public Stream<Service> streamSwarmServices(@NonNull Collection<String> names) {
    val set = new HashSet<>(names);
    return client.listServicesCmd().withNameFilter(List.copyOf(names)).exec().stream()
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

  @SneakyThrows
  public void pullImage(@NonNull String imageName, @NonNull String tag, Terminal t) {
    client.pullImageCmd(imageName)
        .withTag(tag)
        .exec(
        new PullImageResultCallback() {
          @Override
          public synchronized void onNext(PullResponseItem item) {
            super.onNext(item);
            if (item.getProgress() == null) {
              t.resetLine();
              t.print(item.getId() != null ? "[" + item.getId() + "] " + item.getStatus() : item.getStatus());
            }
            if (item.getProgress() != null) {
              t.resetLine();
              t.print("[" + item.getId() + "] " + item.getProgress());
              return;
            }

          }
        }
    ).awaitCompletion();
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

  /** Idempotent method that initializes a swarm cluster */
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

  public DeploymentStates waitForServiceRunning(String serviceName, int numRetries, Duration poll) {
    val service =
        client.listServicesCmd().withNameFilter(List.of(serviceName)).exec().stream()
            .findFirst()
            .get();

    // TODO: clean this up
    long maxAttempts = service.getSpec().getMode().getReplicated().getReplicas();
    if (nonNull(service.getSpec().getTaskTemplate().getRestartPolicy())) {
      maxAttempts = service.getSpec().getTaskTemplate().getRestartPolicy().getMaxAttempts();
    }
    val resolvedMaxAttempt = maxAttempts;
    val retry =
        new RetryPolicy<DeploymentStates>()
            .abortIf(x -> x != INFLIGHT)
            .withDelay(poll)
            .withMaxRetries(numRetries);
    return Failsafe.with(retry).get(() -> resolveServiceState(serviceName, resolvedMaxAttempt));
  }

  private List<String> getContainerIdsForService(String serviceName) {
    return client.listTasksCmd().withServiceFilter(serviceName).exec().stream()
        .map(Task::getStatus)
        .map(TaskStatus::getContainerStatus)
        .filter(Objects::nonNull)
        .map(TaskStatusContainerStatus::getContainerID)
        .filter(Objects::nonNull)
        .collect(toUnmodifiableList());
  }

  private void waitForContainerDeletion(
      Object lock, Collection<String> containerIds, int numRetries, Duration poll) {
    if (!containerIds.isEmpty()) {
      poll(
          lock,
          () -> client.listContainersCmd().withIdFilter(containerIds).exec(),
          List::isEmpty,
          numRetries,
          poll.toMillis());
    }
  }

  @SneakyThrows
  // TODO: delete this
  private DeploymentStates waitForServiceRunning3(
      String serviceName, int numRetries, Duration poll) {
    val service =
        client.listServicesCmd().withNameFilter(List.of(serviceName)).exec().stream()
            .findFirst()
            .get();

    long maxAttempts = service.getSpec().getTaskTemplate().getRestartPolicy().getMaxAttempts();

    return poll(
        this,
        () -> resolveServiceState(serviceName, maxAttempts),
        x -> x != INFLIGHT,
        numRetries,
        poll.toMillis());
  }

  @SneakyThrows
  // TODO: delete this
  private DeploymentStates waitForServiceRunning2(
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

  private DeploymentStates resolveServiceState(String serviceName, long maxReplicas) {
    val resolvedState = internalReadServiceState(serviceName, maxReplicas);
    if (resolvedState == INFLIGHT) {
      log.info("Service {} is not ready yet, waiting", serviceName);
    } else if (resolvedState == UNSUCCESSFUL) {
      log.error(
          "Service {} was UNSUCCESSFUL in deploying properly, even after {} retries",
          serviceName,
          maxReplicas);
      return resolvedState;
    } else if (resolvedState == SUCCESSFUL) {
      log.info("Service {} was SUCCESSFUL in deploying properly", serviceName);
      return resolvedState;
    }
    return resolvedState;
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
