package bio.overture.dms.infra.service;

public interface DeployCallback {

  void onCreate(String containerId);

  void onStart(String containerId);

  void onError(String containerId);
}
