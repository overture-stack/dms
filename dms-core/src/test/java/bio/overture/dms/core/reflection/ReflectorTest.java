package bio.overture.dms.core.reflection;

import bio.overture.dms.core.Factory;
import bio.overture.dms.core.reflection.ReflectorException;
import bio.overture.dms.core.reflection.Reflector;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Test;

import static bio.overture.dms.core.util.Tester.assertEquals;
import static bio.overture.dms.core.util.Tester.assertExceptionThrown;

public class ReflectorTest {

  private static final Reflector REFLECTOR = Factory.buildReflector();

  @Test
  @SneakyThrows
  public void getFieldValue_MissingGetter_ReflectorException(){
    val example = new MissingGetterClass();
    example.setAge(100);
    example.setHeight(200);
    example.setName("John");

    val ageField = MissingGetterClass.class.getDeclaredField("age");
    val heightField = MissingGetterClass.class.getDeclaredField("height");

    assertReflectorException(() -> REFLECTOR.getFieldValue(example.getClass(), example, ageField), "Could not find getter method" );
    assertReflectorException(() -> REFLECTOR.getFieldValue(example.getClass(), example, heightField), "Could not find getter method" );
  }

  @Test
  @SneakyThrows
  public void getFieldValue_Normal_Success(){
    val person = new Person();
    person.setAge(100);
    person.setHeight(200);
    person.setName("John");

    val ageField    = Person.class.getDeclaredField(Person.Fields.age);
    val heightField = Person.class.getDeclaredField(Person.Fields.height);
    val nameField   = Person.class.getDeclaredField(Person.Fields.name);
    val countryField= Person.class.getDeclaredField(Person.Fields.country);

    assertEquals(person.getAge(), REFLECTOR.getFieldValue(person.getClass(), person, ageField));
    assertEquals(person.getHeight(), REFLECTOR.getFieldValue(person.getClass(), person, heightField));
    assertEquals(person.getName(), REFLECTOR.getFieldValue(person.getClass(), person, nameField));
    assertEquals(person.getCountry(), REFLECTOR.getFieldValue(person.getClass(), person, countryField));
  }

  private static void assertReflectorException(@NonNull Runnable runnable, @NonNull String containingTextInMessage){
    assertExceptionThrown(runnable, ReflectorException.class,containingTextInMessage );
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
