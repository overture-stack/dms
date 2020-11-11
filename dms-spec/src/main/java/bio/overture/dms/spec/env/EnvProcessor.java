package bio.overture.dms.spec.env;

import bio.overture.dms.core.Nullable;
import bio.overture.dms.spec.reflection.FieldTypes;
import bio.overture.dms.spec.reflection.Reflector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static bio.overture.dms.spec.env.EnvProcessingException.checkEnvProcessing;
import static bio.overture.dms.spec.reflection.Reflector.isClassPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;

/**
 * Processor used for converting objects to an environment variable map
 */
@Slf4j
@RequiredArgsConstructor
public class EnvProcessor {

  private static final EnvFieldVisitor PROCESS_FIELD_VISITOR = new ProcessEnvFieldVisitor();
  private static final EnvFieldVisitor DUMP_FIELD_VISITOR = new DumpEnvFieldVisitor();

  @NonNull private final Reflector reflector;

  /**
   * Generates an environment variable hashmap by recursively processing field names.
   * If the root object is defined by the Person class below
   * <pre>{@code}
   * public class Person {
   *   private String firstName;
   *   private String lastName
   *   private int age;
   *   private Address billingAddress;
   *
   *   &#64;EnvVariable("SHIPPING_ADDRESS")
   *   private Address shippingAddress
   *
   *   // getters are required
   * }
   *
   * public class Address{
   *   private String country;
   *
   *   &#64;EnvVariable("MUNICIPALITY")
   *   private String city;
   *
   *   // getters are required
   * }
   * </pre>
   *
   * then the rendered environment variables would be:
   * <pre>
   *  FIRSTNAME
   *  LASTNAME
   *  AGE
   *  BILLINGADDRESS_COUNTRY
   *  BILLINGADDRESS_MUNICIPALITY
   *  SHIPPING_ADDRESS_COUNTRY
   *  SHIPPING_ADDRESS_MUNICIPALITY
   * </pre>
   *
   *  The {@link EnvVariable} annotation explicitly defines the environment variable for
   *  the associated field, instead of resolving it using the default convention.
   *
   *  <pre>
   *
   *  </pre>
   *
   *  Rules for generating an environment variable map from an object successfully:
   *  <pre>
   *    - All rendered environment variables must be unique.
   *      With the use of &#64;EnvVariable it is possible for a collision to occur.
   *    - All fields must have a public parameter-less getter method defined
   *    - Fields must not be static
   *
   *  </pre>
   *
   *
   * @param obj The object to parse
   * @return map containing environment variables
   * @throws EnvProcessingException when a env variable collision occurs or when a getter method is not found
   */
  public Map<String, String> generateEnvMap(@NonNull Object obj){
    return internalGenerateEnvMap(obj.getClass(), obj, PROCESS_FIELD_VISITOR);
  }

  /**
   * Similar to the {@link EnvProcessor#generateEnvMap(Object)} method,
   * this method processes an input object and extracts all possible
   * environment variables. This is useful for testing how an object will render.
   *
   *<pre>
   *</pre>
   *
   *Note:
   * For objects with children that are also processable, the children MUST NOT be null.
   * If they are null, the environment variable name will NOT be included in the final list.
   *
   *<pre>
   *</pre>
   *
   * @param obj The object to parse
   * @return set containing all possible environment variable names
   */
  public Set<String> dumpAllEnvVariables(@NonNull Object obj){
    val envMap = internalGenerateEnvMap(obj.getClass(), obj, DUMP_FIELD_VISITOR);
    val orderSet = new TreeSet<String>();
    envMap.keySet().forEach(x -> orderSet.add(x));
    return orderSet;
  }

  public static EnvProcessor createEnvProcessor(@NonNull Reflector reflector) {
    return new EnvProcessor(reflector);
  }

  private Map<String, String> internalGenerateEnvMap(@NonNull Class<?> objClass, @Nullable Object obj,
      EnvFieldVisitor envFieldVisitor){
    checkEnvProcessing(isClassPublic(objClass),
        "Cannot process input object of type '%s' since the class is not publicly accessible",
        obj.getClass().getSimpleName());
    val envMap = new HashMap<String, String>();
    processObject(envMap, null, envFieldVisitor, objClass, obj);
    return envMap;
  }

  private void processObject(@NonNull Map<String, String> envMap, @Nullable String inputPrefix,
      @NonNull EnvFieldVisitor envFieldVisitor, @NonNull Class<?> objClass, @Nullable Object obj){
    stream(objClass.getDeclaredFields())
        .filter(x -> !isStatic(x.getModifiers()))
        .forEachOrdered(field -> processField(envMap, objClass, obj, inputPrefix, field, envFieldVisitor));
  }

  // Note: This is a Depth-First approach
  private void processField(Map<String, String> envMap, @NonNull Class<?> objClass, @Nullable Object obj, @Nullable String inputPrefix,
      Field f, EnvFieldVisitor envFieldVisitor){
    val envName = resolveEnvName(inputPrefix, f);
    checkEnvProcessing(!envMap.containsKey(envName),
        "Cannot process object of type '%s' with field '%s' since a collision was detected with the env variable '%s'",
        objClass.getName(), f.getName(), envName);
    log.debug("Processing field '{}' for object type '{}'", f.getName(), objClass.getName());
    val fieldValue = isNull(obj) ? null : reflector.getFieldValue(objClass, obj, f);
    if (isCustomField(f)){
      // Current envName is the prefix for the sub env variables
      processObject(envMap, envName, envFieldVisitor, f.getType(), fieldValue);
    } else {
      envFieldVisitor.visit(envMap, envName, fieldValue);
    }

  }

  private boolean isCustomField(Field f){
    return !FieldTypes.matchesAny(f);
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
