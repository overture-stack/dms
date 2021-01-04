package bio.overture.dms.compose.tasks.impl;

import bio.overture.dms.compose.tasks.PostDeployTask;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;

@Deprecated
public class EgoApiDeployTask implements PostDeployTask {

  @Override
  public String getTaskName() {
    return "ego-api";
  }

  @Override
  public void executePostDeployTask(@NonNull DmsConfig dmsConfig) {
    // 1 - create DMS group, if DNE
    // 2 - create DMS policy, if DNE
    // 3 - create dms-admin permission, if DNE
  }
}
