package bio.overture.dms.domain;

public interface ComposeManager<T> {

  void deploy(T composeObject);

  void destroy(T composeObject, boolean destroyVolumes);
}
