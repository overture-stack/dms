package bio.overture.dms.compose.deployment;

public interface ComposeManager<T> {

  void deploy(T composeObject);

  void destroy(T composeObject, boolean destroyVolumes);
}
