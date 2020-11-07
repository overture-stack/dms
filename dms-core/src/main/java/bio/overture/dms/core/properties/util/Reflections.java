package bio.overture.dms.core.properties.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Reflections {

  @SneakyThrows
  public static Object invokeMethod(Object obj, Method m, Object ... args){
    return m.invoke(obj, args);
  }

  public static boolean isClassPublic(Object obj){
    return Modifier.isPublic(obj.getClass().getModifiers());
  }

  public static boolean isBooleanType(Field f) {
    val type = f.getType();
    return Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type);
  }

  public static boolean isCollection(Field f) {
    return Collection.class.isAssignableFrom(f.getType());
  }

  public static boolean isIterable(Field f) {
    return Iterable.class.isAssignableFrom(f.getType());
  }
}
