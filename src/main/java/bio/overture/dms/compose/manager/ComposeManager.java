package bio.overture.dms.compose.manager;

public interface ComposeManager<T> {

  void deploy(T composeObject);

  void destroy(T composeObject, boolean destroyVolumes);
}
