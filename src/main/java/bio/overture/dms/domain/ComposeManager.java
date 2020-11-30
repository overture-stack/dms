package bio.overture.dms.domain;

public interface ComposeManager<C extends ComposeObject> {

  void deploy(C composeObject);
}
