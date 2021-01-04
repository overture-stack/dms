package bio.overture.dms.compose.tasks;

import bio.overture.dms.core.model.dmsconfig.DmsConfig;

@Deprecated
public interface PostDeployTask extends DeployTask {

  /**
   * An idempotent task that is called POST deployment of a specific service.
   */
  void executePostDeployTask(DmsConfig dmsConfig);
}
