package bio.overture.dms.cli;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Scanner;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class MainQuestionaire {
  @SneakyThrows
  public static void main(String[] args) {
//    val s = new SongQuestionaire();
//    for (val field : SongQuestionaire.class.getDeclaredFields()) {
//      if (field.isAnnotationPresent(Question.class)) {
//        val question = field.getDeclaredAnnotation(Question.class);
//        val questionString = question.value();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println(question);
//        val userInput = scanner.nextLine();
//        Object value = userInput;
//        if (BOOLEAN.matchesField(field)) {
//          value = Boolean.valueOf(userInput);
//        } else if (BYTE.matchesField(field)) {
//          value = Byte.valueOf(userInput);
//        } else if (SHORT.matchesField(field)) {
//          value = Short.valueOf(userInput);
//        } else if (CHAR.matchesField(field)) {
//          if (userInput.length() > 1) {
//            throw new IllegalAccessException(format("The input \"%s\" is not a character"));
//          }
//          value = userInput.charAt(0);
//        } else if (INT.matchesField(field)) {
//          value = Integer.valueOf(userInput);
//        } else if (LONG.matchesField(field)) {
//          value = Long.valueOf(userInput);
//        } else if (FLOAT.matchesField(field)) {
//          value = Float.valueOf(userInput);
//        } else if (DOUBLE.matchesField(field)) {
//          value = Double.valueOf(userInput);
//        } else if (STRING.matchesField(field)) {
//          value = userInput;
//        } else {
//          throw new IllegalStateException(
//              format(
//                  "Could not process field \"%s\" of type \"%s\"",
//                  field.getName(), field.getType().getSimpleName()));
//        }
//        setFieldValueSafely(field, s, value);
//      }
//    }
    log.info("Sdf");
  }

  @SneakyThrows
  public static void main2(String[] args) {
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
