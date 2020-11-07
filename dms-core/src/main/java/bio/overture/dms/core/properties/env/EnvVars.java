package bio.overture.dms.core.properties.env;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import static bio.overture.dms.core.properties.exceptions.EnvProcessingException.checkEnvProcessing;
import static java.util.Objects.isNull;

/**
 * Utility methods for converting objects to an environment variable map
 */
public class EnvVars {

  /**
   * Generates an environment variable hashmap by recursively searching for properly annotated member variables.
   * This is typically the entry point
   * @param obj The object to parse
   * @return map containing environment variables
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public static Map<String, String> generateEnvVarMap(Object obj){
    return generateEnvVarMap(obj, null);
  }

  /**
   * Generates an environment variable hashmap by recursively searching for properly annotated member variables.
   * @param obj The object to parse
   * @param prefix This defines any prefix any declared environment variable annotations. If null, the there is no prefix
   * @return map containing environment variables
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public static Map<String, String> generateEnvVarMap(Object obj, String prefix){
    val map = new HashMap<String, String>();
    checkEnvProcessing(isClassPublic(obj),
        "Cannot process input object of type '%s' since the class is not publicly accessible", obj.getClass().getSimpleName());
    for (val field : obj.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(EnvVariable.class)) {
        processVariable(field, map, obj, prefix);
      } else if (field.isAnnotationPresent(EnvObject.class)) {
        processObject(field, map, obj, prefix);
      }
    }
    return map;
  }

  private static boolean isClassPublic(Object obj){
    return Modifier.isPublic(obj.getClass().getModifiers());
  }

  private static void processObject(Field field, Map<String,String> map, Object obj,  String prefix){
    val fieldValue = getFieldValueSafely(field, obj);
    if (!isNull(fieldValue)) {
      String newPrefix = prefix;
      if (field.isAnnotationPresent(EnvPrefix.class)) {
        newPrefix += field.getDeclaredAnnotation(EnvPrefix.class).value();
      }
      map.putAll(generateEnvVarMap(fieldValue, newPrefix));
    }
  }

  private static void processVariable(Field field, Map<String,String> map, Object obj,  String prefix){
    val envFieldName = field.getDeclaredAnnotation(EnvVariable.class).value();
    val envFieldValue = getFieldValueSafely(field, obj).toString();
    if(!isNull(envFieldValue)){
      map.put(envFieldName, isNull(prefix)? envFieldValue : prefix+envFieldValue );
    }
  }

  @SneakyThrows
  private static void setFieldValueSafely(Field f, Object obj, Object value){
    val setterName = resolveSetterName(f);
    val methodRef = obj.getClass().getDeclaredMethod(setterName, f.getType());
    methodRef.invoke(obj, value);
  }

  @SneakyThrows
  private static Object getFieldValueSafely(Field f, Object obj){
    val getterName = resolveGetterName(f);
    val methodRef = obj.getClass().getDeclaredMethod(getterName);
    return methodRef.invoke(obj);
  }

  private static boolean isBooleanType(Field f) {
    val type = f.getType();
    return type.equals(Boolean.class) || type.equals(boolean.class);
  }

  private static boolean isLongType(Field f) {
    val type = f.getType();
    return type.equals(Long.class) || type.equals(long.class);
  }

  private static boolean isCharType(Field f) {
    val type = f.getType();
    return type.equals(Character.class) || type.equals(char.class);
  }

  @RequiredArgsConstructor
  private enum FieldTypes {
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

  private static boolean isIntegerType(Field f) {
    val type = f.getType();
    return type.equals(Integer.class) || type.equals(int.class);
  }

  private static String resolveSetterName(Field f) {
    val rootName = capitalizeFirstChar(f.getName());
    return "set" + rootName;
  }

  private static String resolveGetterName(Field f) {
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
