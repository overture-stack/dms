package bio.overture.dms.reflection;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.reflections.Reflections;

@RequiredArgsConstructor
public class Reflector {

  @NonNull private final Reflections reflections;

  @SneakyThrows
  public Object getFieldValue(@NonNull Class<?> objClass, @NonNull Object obj, @NonNull Field f) {
    ReflectorException.checkReflection(
        obj.getClass() == objClass,
        "The objClass parameter '%s' and obj parameter's class '%s' do not match",
        objClass.getName(),
        obj.getClass().getName());
    return getGetterMethod(objClass, f).invoke(obj);
  }

  public Method getGetterMethod(@NonNull Class<?> objClass, @NonNull Field f) {
    val result = findGetterMethod(f);
    ReflectorException.checkReflection(
        result.isPresent(),
        "Could not find getter method for field '%s' for object type '%s'",
        f.getName(),
        objClass.getName());
    return result.get();
  }

  public Optional<Method> findGetterMethod(@NonNull Field f) {
    return reflections.getFieldUsage(f).stream()
        .filter(x -> x.getModifiers() == Modifier.PUBLIC)
        .filter(x -> x instanceof Method)
        .map(x -> (Method) x)
        .filter(x -> x.getParameterCount() == 0) // Getter
        .findFirst();
  }

  public static Reflector createReflector(@NonNull Reflections reflections) {
    return new Reflector(reflections);
  }

  public static boolean isClassPublic(Class<?> clazz) {
    return isPublic(clazz.getModifiers());
  }
}
