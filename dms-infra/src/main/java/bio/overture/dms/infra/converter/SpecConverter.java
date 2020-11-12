package bio.overture.dms.infra.converter;

public interface SpecConverter<S, D> {
  D convertSpec(S spec);
}
