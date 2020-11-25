package bio.overture.dms.infra.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmptyDeployCallback implements DeployCallback {

  @Override
  public void onCreate(String containerId) {
    log.info("Created container {}", containerId);
  }

  @Override
  public void onStart(String containerId) {
    log.info("Started container {}", containerId);
  }

  @Override
  public void onError(String containerId) {
    log.info("Error starting container {}", containerId);
  }
}
