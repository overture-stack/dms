package bio.overture.dms.docker.reflection;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.function.Function;

import static java.util.Arrays.stream;

@RequiredArgsConstructor
public enum FieldTypes {
  STRING(FieldTypes::isString),
  BOOLEAN(FieldTypes::isBoolean),
  INT(FieldTypes::isInteger),
  LONG(FieldTypes::isLong),
  CHAR(FieldTypes::isCharacter),
  BYTE(FieldTypes::isByte),
  SHORT(FieldTypes::isShort),
  DOUBLE(FieldTypes::isDouble),
  FLOAT(FieldTypes::isFloat);

  private final Function<Class<?>, Boolean> func;

  public static boolean matchesAny(@NonNull Field f) {
    return stream(FieldTypes.values())
        .anyMatch(x -> x.matchesField(f));
  }

  public boolean matchesField(@NonNull Field f) {
    return this.func.apply(f.getType());
  }

  public static boolean isBoolean(Class<?> c) {
    return Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c);
  }

  public static boolean isFloat(Class<?> c) {
    return Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c);
  }

  public static boolean isInteger(Class<?> c) {
    return Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c);
  }

  public static boolean isLong(Class<?> c) {
    return Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c);
  }

  public static boolean isCharacter(Class<?> c) {
    return Character.class.isAssignableFrom(c) || char.class.isAssignableFrom(c);
  }

  public static boolean isShort(Class<?> c) {
    return Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c);
  }

  public static boolean isByte(Class<?> c) {
    return Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c);
  }

  public static boolean isDouble(Class<?> c) {
    return Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c);
  }

  public static boolean isString(Class<?> c) {
    return String.class.isAssignableFrom(c);
  }

}
