package bio.overture.dms.infra.converter;

@FunctionalInterface
public interface Converter<I, O> {
  O convert(I input);
}
