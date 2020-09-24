package bio.overture.dms.core.properties.service.env;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class EnvVars {

  public static Map<String, String> generateEnvVarMap(Object obj)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    val map = new HashMap<String, String>();
    for (val field : obj.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(EnvVariable.class)) {
        val envFieldName = field.getDeclaredAnnotation(EnvVariable.class).value();
        val envFieldValue = getFieldValueSafely(field, obj).toString();
        map.put(envFieldName, envFieldValue);
      } else if (field.isAnnotationPresent(EnvObject.class)) {
        val fieldValue = getFieldValueSafely(field, obj);
        map.putAll(generateEnvVarMap(fieldValue));
      }
    }
    return map;
  }

  public static void setFieldValueSafely(Field f, Object obj, Object value)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    val setterName = resolveSetterName(f);
    val methodRef = obj.getClass().getDeclaredMethod(setterName, f.getType());
    methodRef.invoke(obj, value);
  }

  public static Object getFieldValueSafely(Field f, Object obj)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    val getterName = resolveGetterName(f);
    val methodRef = obj.getClass().getDeclaredMethod(getterName);
    return methodRef.invoke(obj);
  }

  public static boolean isBooleanType(Field f) {
    val type = f.getType();
    return type.equals(Boolean.class) || type.equals(boolean.class);
  }

  public static boolean isLongType(Field f) {
    val type = f.getType();
    return type.equals(Long.class) || type.equals(long.class);
  }

  public static boolean isCharType(Field f) {
    val type = f.getType();
    return type.equals(Character.class) || type.equals(char.class);
  }

  @RequiredArgsConstructor
  public enum FieldTypes {
    BOOLEAN(t -> t.equals(Boolean.class) || t.equals(boolean.class)),
    FLOAT(t -> t.equals(Float.class) || t.equals(float.class)),
    INT(t -> t.equals(Integer.class) || t.equals(int.class)),
    LONG(t -> t.equals(Long.class) || t.equals(long.class)),
    CHAR(t -> t.equals(Character.class) || t.equals(char.class)),
    SHORT(t -> t.equals(Short.class) || t.equals(short.class)),
    BYTE(t -> t.equals(Byte.class) || t.equals(byte.class)),
    DOUBLE(t -> t.equals(Double.class) || t.equals(double.class)),
    STRING(t -> t.equals(String.class));

    private final Function<Class<?>, Boolean> func;

    public boolean matchesField(Field f) {
      return this.func.apply(f.getType());
    }
  }

  public static boolean isIntegerType(Field f) {
    val type = f.getType();
    return type.equals(Integer.class) || type.equals(int.class);
  }

  public static String resolveSetterName(Field f) {
    val rootName = capitalizeFirstChar(f.getName());
    return "set" + rootName;
  }

  public static String resolveGetterName(Field f) {
    val rootName = capitalizeFirstChar(f.getName());
    if (isBooleanType(f)) {
      return "is" + rootName;
    } else {
      return "get" + rootName;
    }
  }

  private static String capitalizeFirstChar(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }
}
