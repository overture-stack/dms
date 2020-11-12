package bio.overture.dms.infra.service;

import bio.overture.dms.infra.spec.DmsSpec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DmsDeploymentService {

  @NonNull private final EgoDeploymentService egoDeploymentService;

  public void deploy(@NonNull DmsSpec dmsSpec){
    egoDeploymentService.deployEgo(dmsSpec.getEgo());
  }

}
