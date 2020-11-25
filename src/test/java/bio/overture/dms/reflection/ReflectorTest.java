package bio.overture.dms.reflection;

import static bio.overture.dms.core.Tester.assertExceptionThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.dms.config.EnvConfig;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Test;

public class ReflectorTest {

  private static final Reflector REFLECTOR;

  static {
    val c = new EnvConfig();
    REFLECTOR = c.buildReflector(c.buildReflections());
  }

  @Test
  @SneakyThrows
  public void getFieldValue_MissingGetter_ReflectorException() {
    val example = new MissingGetterClass();
    example.setAge(100);
    example.setHeight(200);
    example.setName("John");

    val ageField = MissingGetterClass.class.getDeclaredField("age");
    val heightField = MissingGetterClass.class.getDeclaredField("height");

    assertReflectorException(
        () -> REFLECTOR.getFieldValue(example.getClass(), example, ageField),
        "Could not find getter method");
    assertReflectorException(
        () -> REFLECTOR.getFieldValue(example.getClass(), example, heightField),
        "Could not find getter method");
  }

  @Test
  @SneakyThrows
  public void getFieldValue_Normal_Success() {
    val person = new Person();
    person.setAge(100);
    person.setHeight(200);
    person.setName("John");

    val ageField = Person.class.getDeclaredField(Person.Fields.age);
    val heightField = Person.class.getDeclaredField(Person.Fields.height);
    val nameField = Person.class.getDeclaredField(Person.Fields.name);
    val countryField = Person.class.getDeclaredField(Person.Fields.country);

    assertEquals(person.getAge(), REFLECTOR.getFieldValue(person.getClass(), person, ageField));
    assertEquals(
        person.getHeight(), REFLECTOR.getFieldValue(person.getClass(), person, heightField));
    assertEquals(person.getName(), REFLECTOR.getFieldValue(person.getClass(), person, nameField));
    assertEquals(
        person.getCountry(), REFLECTOR.getFieldValue(person.getClass(), person, countryField));
  }

  private static void assertReflectorException(
      @NonNull Runnable runnable, @NonNull String containingTextInMessage) {
    assertExceptionThrown(runnable, ReflectorException.class, containingTextInMessage);
  }

  @Data
  @FieldNameConstants
  public static class Person {
    private String name;
    private Integer age;
    private int height;
    private String country;
  }

  public static class MissingGetterClass {
    @Getter @Setter private String name;
    @Setter private Integer age;
    @Setter private int height;
  }
}
