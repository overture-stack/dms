package bio.overture.dms.compose.tasks;

import bio.overture.dms.core.model.dmsconfig.DmsConfig;

public interface PreDeployTask extends DeployTask {

  /**
   * An idempotent task that is called PRE deployment of a specific service.
   */
  void executePreDeployTask(DmsConfig dmsConfig);
}
