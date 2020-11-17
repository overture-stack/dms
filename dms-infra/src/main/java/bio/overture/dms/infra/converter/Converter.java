package bio.overture.dms.infra.converter;

public interface Converter<I, O> {
  O convert(I input);
}
