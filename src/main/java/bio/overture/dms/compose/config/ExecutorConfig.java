package bio.overture.dms.compose.config;

import bio.overture.dms.compose.properties.AsyncProperties;
import bio.overture.dms.core.util.SpringExecutorService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.ForkJoinPool.commonPool;

@Configuration
public class ExecutorConfig {

  private final AsyncProperties asyncProperties;

  @Autowired
  public ExecutorConfig(@NonNull AsyncProperties asyncProperties) {
    this.asyncProperties = asyncProperties;
  }

  @Bean
  public ExecutorService executor() {
    ExecutorService internalExecutorService = commonPool();
    if (asyncProperties.getThreadCount() > 0 ){
      internalExecutorService = newFixedThreadPool(asyncProperties.getThreadCount());
    }
    return new SpringExecutorService(internalExecutorService, asyncProperties.getTimeoutDuration());
  }

}
