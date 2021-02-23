package bio.overture.dms.compose.deployment;

import static bio.overture.dms.compose.deployment.ServiceDeployer.DeployTypes.CREATE;
import static bio.overture.dms.compose.deployment.ServiceDeployer.DeployTypes.UPDATE;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.rest.RestClientFactory;
import bio.overture.dms.swarm.service.SwarmService;
import com.github.dockerjava.api.model.ServiceSpec;
import java.time.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

/** Deploys a DMS service */
@Slf4j
@Component
public class ServiceDeployer {

  /** Constants */
  private static final int NUM_RETRIES = 300;

  private static final Duration POLL_PERIOD = Duration.ofSeconds(2);

  /** Dependencies */
  private final SwarmService swarmService;

  private final Messenger messenger;
  private final ServiceSpecRenderEngine serviceSpecRenderEngine;
  private final RestClientFactory restClient;

  private static final RetryPolicy<Boolean> RETRY_POLICY =
      new RetryPolicy<Boolean>().withMaxRetries(5).withDelay(Duration.ofSeconds(5));

  @Autowired
  public ServiceDeployer(
      @NonNull SwarmService swarmService,
      @NonNull Messenger messenger,
      @NonNull ServiceSpecRenderEngine serviceSpecRenderEngine,
      @NonNull @Qualifier("nonRetryingRestClientFactory") RestClientFactory restClient) {
    this.swarmService = swarmService;
    this.messenger = messenger;
    this.serviceSpecRenderEngine = serviceSpecRenderEngine;
    this.restClient = restClient;
  }

  public DeployTypes deploy(
      @NonNull DmsConfig dmsConfig,
      @NonNull ComposeServiceResources composeServiceResource,
      boolean waitForServiceRunning) {
    val serviceSpec =
        serviceSpecRenderEngine.render(dmsConfig, composeServiceResource).orElseThrow();
    val deployType = deployServiceSpec(serviceSpec);
    if (waitForServiceRunning) {
      waitForServiceRunning(serviceSpec);
    }
    return deployType;
  }

  // TODO: not working properly. Is not waiting for service to be RUNNING. Specifically, ego-db says
  // "waiting" and then doesnt show "now running"
  public void waitForServiceRunning(@NonNull ServiceSpec s) {
    //    messenger.send("⏳ Waiting for the service '%s' to be in the RUNNING state", s.getName());
    swarmService.waitForServiceRunning(s.getName(), NUM_RETRIES, POLL_PERIOD);
    //    messenger.send("✔️ Service %s started", s.getName());
  }

  private DeployTypes deployServiceSpec(@NonNull ServiceSpec s) {
    //    messenger.send("⏳ Deploying service '%s' ...", s.getName());
    val out = deploySwarmService(s);
    //    messenger.send("✔️ Deployed service %s", s.getName());
    return out;
  }

  private DeployTypes deploySwarmService(ServiceSpec s) {
    swarmService.ping();
    val result = swarmService.findSwarmServiceInfo(s.getName(), true);
    if (result.isPresent()) {
      log.debug("Found service '{}' info, updating existing service spec", s.getName());
      val info = result.get();
      swarmService.updateSwarmService(info.getId(), s, info.getVersion());
      return UPDATE;
    } else {
      log.debug("Service '{}' info was NOT found, create new service spec", s.getName());
      swarmService.createSwarmService(s);
      return CREATE;
    }
  }

  public enum DeployTypes {
    UPDATE,
    CREATE;
  }

  public static void waitForOk(String url, String basicAuth) {
    val basicAuthHeaderValue = "basic " + new String(Base64Utils.encode(basicAuth.getBytes()));
    Failsafe.with(RETRY_POLICY)
        .get(
            () -> {
              val client = new OkHttpClient.Builder().build();
              val reqBuilder = new Request.Builder().url(url).get();
              if (!StringUtils.isEmpty(basicAuth)) {
                reqBuilder.addHeader("Authorization", basicAuthHeaderValue);
              }
              val response = client.newCall(reqBuilder.build()).execute();
              System.out.println(
                  "recieved response "
                      + response.body().string()
                      + " and status "
                      + response.code());
              return response.code() == 200;
            });
  }
}
