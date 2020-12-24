package bio.overture.dms.ego.config;

import static java.time.temporal.ChronoUnit.SECONDS;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.properties.EgoClientProperties;
import bio.overture.dms.ego.properties.RetryProperties;
import bio.overture.dms.rest.okhttp.OkHttpException;
import bio.overture.dms.rest.okhttp.OkHttpRestClientFactory;
import java.net.ConnectException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EgoClientConfig {
  private static final int SERVICE_UNAVAILABLE_CODE = 503;

  @Autowired private EgoClientProperties egoClientProperties;

  @Bean
  public OkHttpRestClientFactory okHttpRestClientFactory(
      @Autowired ObjectSerializer jsonSerializer) {
    val timeoutProps = egoClientProperties.getTimeoutSeconds();
    return OkHttpRestClientFactory.builder()
        .callTimeout(timeoutProps.getCallDuration())
        .connectTimeout(timeoutProps.getConnectDuration())
        .readTimeout(timeoutProps.getReadDuration())
        .writeTimeout(timeoutProps.getWriteDuration())
        .jsonSerializer(jsonSerializer)
        .retryPolicy(buildRetryPolicy(egoClientProperties.getRetry()))
        .build();
  }

  private static RetryPolicy<String> buildRetryPolicy(RetryProperties retryProperties) {
    return new RetryPolicy<String>()
        .abortOn(t -> !isRetryableException(t))
        .onRetry(
            x ->
                log.error(
                    "retry {} in the last {} seconds: {}",
                    x.getAttemptCount(),
                    x.getElapsedTime().toSeconds(),
                    x.toString()))
        .withMaxAttempts(retryProperties.getMaxAttempts())
        .withBackoff(
            retryProperties.getInitialSeconds(),
            retryProperties.getMaxSeconds(),
            SECONDS,
            retryProperties.getMultiplier());
  }

  private static boolean isRetryableException(Throwable t) {
    if (t instanceof OkHttpException) {
      val okHttpException = (OkHttpException) t;
      return okHttpException.getResponse().code() == SERVICE_UNAVAILABLE_CODE;
    } else if (t instanceof ConnectException) {
      return true;
    }
    return false;
  }
}
