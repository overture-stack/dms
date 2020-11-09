package bio.overture.dms.core.properties.env;

import bio.overture.dms.core.properties.util.Nullable;
import bio.overture.dms.core.properties.util.Reflections2;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static bio.overture.dms.core.properties.env.EnvFieldProcessor.EnvContext.createEnvContext;
import static bio.overture.dms.core.properties.exceptions.EnvProcessingException.buildEnvProcessingException;
import static bio.overture.dms.core.properties.exceptions.EnvProcessingException.checkEnvProcessing;
import static bio.overture.dms.core.properties.util.CollectionUtils.intersection;
import static bio.overture.dms.core.properties.util.Joiner.COMMA;
import static bio.overture.dms.core.properties.util.Reflections2.invokeMethod;
import static bio.overture.dms.core.properties.util.Reflections2.isCollection;
import static bio.overture.dms.core.properties.util.Reflections2.isIterable;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Processor used for converting objects to an environment variable map
 */
@RequiredArgsConstructor
public class EnvFieldProcessor {

  @NonNull private final Class<?> clazz;
  @NonNull private final Map<String, Method> methodMap;
  @NonNull private final Map<String, Field> envVariableMap;
  @NonNull private final Map<Field, String> envObjectMap;

  /**
   * Generates an environment variable hashmap by recursively searching for properly annotated member variables.
   * This is typically the entry point
   * @param obj The object to parse
   * @return map containing environment variables
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public Map<String, String> generateEnvVarMap(Object obj){
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
  public Map<String, String> generateEnvVarMap(@NonNull Object obj, @Nullable String prefix){
    checkEnvProcessing(Reflections2.isClassPublic(obj),
        "Cannot process input object of type '%s' since the class is not publicly accessible", obj.getClass().getSimpleName());

    val map = processEnvVariables(prefix, obj);
    processEnvObjectsRecursively(map, prefix, obj);

    // Remove null values
    return map.entrySet().stream()
        .filter(e -> !isNull(e.getValue()))
        .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
  }

  public boolean isEnvVariable(@NonNull Field f){
    return envVariableMap.containsValue(f);
  }

  public boolean isEnvObject(@NonNull Field f){
    return envObjectMap.containsKey(f);
  }

  public boolean hasMethod(@NonNull String methodName){
    return methodMap.containsKey(methodName);
  }

  public static EnvFieldProcessor createFieldProcessor(@NonNull Class<?> clazz){
    val methodMap = stream(clazz.getDeclaredMethods())
        .collect(toMap(Method::getName, identity()));

    val envVariableMap = stream(clazz.getDeclaredFields())
        .filter(EnvFieldProcessor::isEnvVariableField)
        .collect(toMap(EnvFieldProcessor::extractEnvVariableName, identity()));


    val envObjectMap = new HashMap<Field, String>();
    stream(clazz.getDeclaredFields())
        .filter(EnvFieldProcessor::isEnvObjectField)
        .forEach(f -> envObjectMap.put(f, getEnvPrefixField(f)));
    return new EnvFieldProcessor(clazz, methodMap, envVariableMap, envObjectMap);
  }


  private Map<String, String> processEnvVariables(@Nullable String inputPrefix, Object obj){
    val map = new HashMap<String, String>();
    val envContextMap = generateEnvContextMap();
    envContextMap.forEach((field, envContext) -> {
      val envFieldName = envContext.getEnvName();
      // TODO: test env name collisions
      checkEnvProcessing(!map.containsKey(envFieldName),
          "Cannot process object of type '%s' with field '%s' since the envVariable '%s' was already defined somewhere",
          obj.getClass().getName(), field.getName(), envFieldName);
      val envFieldValue = envContext.getFieldValueSafely(obj).toString();
      map.put(envFieldName, isNull(inputPrefix) ? envFieldValue : inputPrefix+envFieldValue );
    });
    return map;
  }

  private void processEnvObjectsRecursively(Map<String, String> inputMap, @Nullable String inputPrefix, Object obj ){
    envObjectMap.forEach((field, newPrefix) -> {
      val finalPrefix = (isNull(inputPrefix) ? "" : inputPrefix) + (isNull(newPrefix) ? "" : newPrefix);
      findGetterMethod(field)
          .map(method -> invokeMethod(obj, method))
          .ifPresent(value -> {
            val resultMap = createFieldProcessor(value.getClass())
                .generateEnvVarMap(value, finalPrefix);
            val newEnvVarNames = resultMap.keySet();
            val existingEnvVarNames = inputMap.keySet();
            val collisions = intersection(newEnvVarNames, existingEnvVarNames);
            checkEnvProcessing(collisions.isEmpty(),
                "The envObject of type '%s' with field '%s' cannot be processed since the following "
                    + "env variable names were previously defined somewhere: [%s]",
                value.getClass().getName(), field.getName(), COMMA.join(collisions));
            inputMap.putAll(resultMap);
          });
    });
  }

  private Map<Field, EnvContext> generateEnvContextMap(){
    val map = new HashMap<Field,EnvContext>();
    envVariableMap .forEach((envVarName, field) ->
        findGetterMethod(field)
            .map(method -> createEnvContext(envVarName, method))
            .ifPresentOrElse(envContext -> map.put(field, envContext),
                //TODO: add test for missing methods for envVariable annotated fields
                () -> {
                  throw buildEnvProcessingException(
                      "No corresponding getter method was found for envVariable field '%s' for object type '%s'",
                      field.getName(), clazz.getName() );
                }));
    return Map.copyOf(map);
  }


  private Optional<Method> findGetterMethod(Field f) {
    checkValidFieldType(f);
    val rootName = capitalizeFirstChar(f.getName());
    if (Reflections2.isBooleanType(f)) {
      val result = findMethod("is"+rootName, f.getType());
      if (result.isPresent()){
        return result;
      }
    }
    return findMethod("get"+rootName, f.getType());
  }

  private static String capitalizeFirstChar(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private <T> Optional<Method> findMethod(String methodName, Class<T> returnType){
    if (hasMethod(methodName)){
      val method = methodMap.get(methodName);
      if (method.getReturnType() == returnType){
        return Optional.of(method);
      }
    }
    return Optional.empty();
  }

  // TODO: test anything that extends collections and iterables
  private static void checkValidFieldType(Field f){
    val formattedMessage =  "The field '%s' of type '%s' cannot be processed since %s types are not supported";
    checkEnvProcessing(!isCollection(f), formattedMessage, f.getName(), f.getType(), "Collection");
    checkEnvProcessing(!isIterable(f), formattedMessage , f.getName(), f.getType(), "Iterable");
  }

  private static String extractEnvVariableName(Field f){
    return f.getDeclaredAnnotation(EnvVariable.class).value();
  }

  private static String getEnvPrefixField(Field f){
    return isEnvPrefixField(f) ? f.getDeclaredAnnotation(EnvPrefix.class).value() : null;
  }

  private static boolean isEnvPrefixField(Field f){
    return f.isAnnotationPresent(EnvPrefix.class);
  }

  private static boolean isEnvVariableField(Field f){
    return f.isAnnotationPresent(EnvVariable.class);
  }

  private static boolean isEnvObjectField(Field f){
    return f.isAnnotationPresent(EnvObject.class);
  }


  @Value
  public static class EnvContext {
    @NonNull private final String envName;
    @NonNull private final Method getterMethod;

    @SneakyThrows
    public Object getFieldValueSafely(Object obj){
      return getterMethod.invoke(obj);
    }

    public static EnvContext createEnvContext(String envName, Method getterMethod){
      return new EnvContext(envName, getterMethod);
    }
  }

}
