package bio.overture.dms.core.properties.env;

import bio.overture.dms.core.properties.exceptions.EnvProcessingException;
import bio.overture.dms.core.properties.util.Nullable;
import bio.overture.dms.core.properties.util.Reflections2.FieldTypes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static bio.overture.dms.core.properties.exceptions.EnvProcessingException.buildEnvProcessingException;
import static bio.overture.dms.core.properties.exceptions.EnvProcessingException.checkEnvProcessing;
import static bio.overture.dms.core.properties.util.Reflections2.invokeMethod;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class EnvProcessor {

  @NonNull private final Reflections reflections;

  public Map<String, String> generateEnvMap(@NonNull Object obj){
    val envMap = new HashMap<String, String>();
    processObject(envMap, obj,null);
    return Map.copyOf(envMap);
  }

  private void processObject(@NonNull Map<String, String> envMap, @Nullable Object obj, @Nullable String inputPrefix){
    if (!isNull(obj)){
      stream(obj.getClass().getDeclaredFields())
          .filter(x -> !isStatic(x.getModifiers()))
          .forEachOrdered(field -> processField(envMap, obj, inputPrefix, field));
    }
  }

  // Note: This is a Depth-First approach
  private void processField(Map<String, String> envMap, @Nullable Object obj, @Nullable String inputPrefix, Field f){
    val envName = resolveEnvName(inputPrefix, f);
    checkEnvProcessing(!envMap.containsKey(envName),
        "Cannot process object of type '%s' with field '%s' since the env variable '%s' was already defined somewhere",
        obj.getClass().getName(), f.getName(), envName);
    val fieldValue = getFieldValueForObject(f, obj);
    if (isCustomField(f)){
      // Current envName is the prefix for the sub env variables
      processObject(envMap, fieldValue, envName);
    } else {
      envMap.put(envName, fieldValue.toString());
    }
  }

  private boolean isCustomField(Field f){
    return !FieldTypes.matchesAny(f);
  }

  private Object getFieldValueForObject(Field f, Object obj){
    val result =  reflections.getFieldUsage(f).stream()
        .filter(x -> x.getModifiers() == Modifier.PUBLIC)
        .filter(x -> x instanceof Method)
        .map(x -> (Method)x)
        .filter(x -> x.getParameterCount() == 0) //Getter
        .findFirst();
    checkEnvProcessing(result.isPresent(),"Could not find getter method for field '%s' for object type '%s'",
        f.getName(), obj.getClass().getName());
    return invokeMethod(obj, result.get());
  }

  private static String resolveEnvName(@Nullable String inputPrefix, Field f){
    val baseName = f.isAnnotationPresent(EnvVariable.class) ?
        f.getAnnotation(EnvVariable.class).value() : convertFieldNameToEnvName(f);
    return isNull(inputPrefix) ? baseName: inputPrefix+"_"+baseName;
  }

  private static String convertFieldNameToEnvName(Field f){
    return f.getName().toUpperCase();
  }

}
