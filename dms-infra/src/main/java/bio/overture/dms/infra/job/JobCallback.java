package bio.overture.dms.infra.job;

public interface JobCallback {

  void onDone(DeployJob job);
  void onError(DeployJob job);
}
