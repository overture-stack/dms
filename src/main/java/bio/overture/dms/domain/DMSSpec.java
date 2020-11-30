package bio.overture.dms.domain;

public interface DMSSpec<E extends EGOSpec> {
  String getVersion();

  E getEgo();
}
