package bio.overture.dms.cli;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Main {

  @SneakyThrows
  public static void main(String[] args) {
    val ex = Config.builder().firstName("Robert").lastName("Tisma").age(5).build();
    for (val field : Config.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(Question.class)) {
        val question = field.getDeclaredAnnotation(Question.class).value();
        val getterName =
            "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        val methodRef = Config.class.getDeclaredMethod(getterName);
        val value = methodRef.invoke(ex);
        log.info(question + " ----  " + value);
      }
    }
    log.info("sdf");
  }

  public static class QuestionDTO<A, T> {

    @NonNull private final String text;
    @NonNull private final Class<T> fieldType;
    @NonNull private final Method getterMethod;
    @NonNull private final Method setterMethod;

    @Builder
    public QuestionDTO(
        @NonNull String text,
        @NonNull Class<T> fieldType,
        @NonNull Method getterMethod,
        @NonNull Method setterMethod) {
      this.text = text;
      this.fieldType = fieldType;
      this.getterMethod = getterMethod;
      this.setterMethod = setterMethod;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T getValue(A object) {
      return (T) getterMethod.invoke(object);
    }

    @SneakyThrows
    public void setValue(A object, T value) {
      setterMethod.invoke(object, value);
    }
  }
}
