package bio.overture.dms.docker.env;

import bio.overture.dms.docker.spec.Factory;
import bio.overture.dms.core.Nullable;
import bio.overture.dms.test.Tester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static bio.overture.dms.test.Tester.assertExceptionThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class EnvTest {
  private static final String LASTNAME = "LASTNAME";
  private static final String AGE = "AGE";
  private static final String MALE = "MALE";
  private static final String HEIGHT= "HEIGHT";
  private static final String WEIGHT = "WEIGHT";
  private static final String MYCHAR = "MYCHAR";
  private static final String NUM_CHILDREN = "NUM_CHILDREN";
  private static final String HAS_HOUSE = "HAS_HOUSE";
  private static final String INCOME = "INCOME";
  private static final String COMPANY = "COMPANY";

  private static final String PERSON2_LASTNAME = "PERSON2_LASTNAME";
  private static final String PERSON2_HEIGHT= "PERSON2_HEIGHT";
  private static final String PERSON2_AGE= "PERSON2_AGE";
  private static final String PERSON2_MALE= "PERSON2_MALE";
  private static final String PERSON2_MYCHAR= "PERSON2_MYCHAR";
  private static final String PERSON2_WEIGHT= "PERSON2_WEIGHT";

  private static final String PERSON_LASTNAME = "PERSON_LASTNAME";
  private static final String PERSON_HEIGHT= "PERSON_HEIGHT";
  private static final String PERSON_AGE= "PERSON_AGE";
  private static final String PERSON_MALE= "PERSON_MALE";
  private static final String PERSON_MYCHAR= "PERSON_MYCHAR";
  private static final String PERSON_WEIGHT= "PERSON_WEIGHT";

  private static final String PERSON_NUM_CHILDREN = "PERSON_NUM_CHILDREN";
  private static final String PERSON_HAS_HOUSE = "PERSON_HAS_HOUSE";
  private static final String PERSON_INCOME = "PERSON_INCOME";
  private static final String PERSON2_NUM_CHILDREN = "PERSON2_NUM_CHILDREN";
  private static final String PERSON2_HAS_HOUSE = "PERSON2_HAS_HOUSE";
  private static final String PERSON2_INCOME = "PERSON2_INCOME";
  private static final String TEST_PERSON2_LASTNAME = "TEST_PERSON2_LASTNAME";
  private static final String TEST_PERSON2_AGE = "TEST_PERSON2_AGE";
  private static final String TEST_PERSON2_MALE = "TEST_PERSON2_MALE";
  private static final String TEST_PERSON2_HEIGHT = "TEST_PERSON2_HEIGHT";
  private static final String TEST_PERSON2_WEIGHT = "TEST_PERSON2_WEIGHT";
  private static final String TEST_PERSON2_MYCHAR = "TEST_PERSON2_MYCHAR";
  private static final String TEST_PERSON2_NUM_CHILDREN = "TEST_PERSON2_NUM_CHILDREN";
  private static final String TEST_PERSON2_HAS_HOUSE = "TEST_PERSON2_HAS_HOUSE";
  private static final String TEST_PERSON2_INCOME = "TEST_PERSON2_INCOME";

  private static final EnvProcessor ENV_PROCESSOR = Factory.buildEnvProcessor();

  @Test
  public void generateEnvMap_Private_EnvProcessingException(){
    assertEnvProcessingException(new PrivateClass(),"class is not publicly accessible");
  }

  @Test
  public void generateEnvMap_NullCustomChildren_Success(){
    val person2 = Person.builder()
        .lastName("Doe")
        .build();
    val employee = Employee.builder()
        .person(null)
        .person2(person2)
        .person3(null)
        .testPerson(null)
        .company("OICR")
        .build();
    val map= ENV_PROCESSOR.generateEnvMap(employee);
    assertTrue(map.containsKey(COMPANY));
    assertTrue(map.containsKey(PERSON2_LASTNAME));
    assertEquals(7, map.size());
    assertEquals("OICR", map.get(COMPANY));
    assertEquals("Doe", map.get(PERSON2_LASTNAME));
    assertEquals("0", map.get(PERSON2_HEIGHT));
    assertEquals("0", map.get(PERSON2_AGE));
    assertEquals("false", map.get(PERSON2_MALE));
    assertEquals(Character.toString(0), map.get(PERSON2_MYCHAR));
    assertEquals("0.0", map.get(PERSON2_WEIGHT));
  }


  @Test
  public void dumpEnvs_NonNested_Success(){
    val person= new Person();
    val envVars =  ENV_PROCESSOR.dumpAllEnvVariables(person);

    Stream.of(
        LASTNAME,
        AGE, MALE,
        HEIGHT,
        WEIGHT,
        MYCHAR,
        NUM_CHILDREN,
        HAS_HOUSE,
        INCOME )
        .forEach(x -> Tester.assertTrue(envVars.contains(x),
            "Expected env variable '%s' does not exist in the env vars dump", x ));
  }

  @Test
  public void dumpEnvs_Nested_Success(){
    val employee = Employee.builder()
        .person(new Person())
        .person2(new Person())
        .testPerson(new Person())
        .person3(new Person())
        .build();

    val envVars =  ENV_PROCESSOR.dumpAllEnvVariables(employee);
    val fieldsPerPerson = 9;
    val numPersonFieldsPerEmployee = 4;
    val numEmployeeFields = 1;
    val totalExpectedEnvVariables = numPersonFieldsPerEmployee*fieldsPerPerson + numEmployeeFields;
    assertEquals(totalExpectedEnvVariables, envVars.size());
    assertTrue(envVars.contains(TEST_PERSON2_AGE));
    assertTrue(envVars.contains(TEST_PERSON2_HAS_HOUSE));
    assertTrue(envVars.contains(TEST_PERSON2_WEIGHT));
  }

  @Test
  public void generateEnvMap_Employee_Success(){
    val person= Person.builder()
        .lastName("Doe")
        .age(90)
        .male(true)
        .height(180L)
        .weight(200.0)
        .myChar('q')
        .numChildren(4)
        .hasHouse(true)
        .income(50000L)
        .build();

    val person2= Person.builder()
        .lastName("Doe2")
        .age(92)
        .male(false)
        .height(182L)
        .weight(202.0)
        .myChar('r')
        .numChildren(5)
        .hasHouse(false)
        .income(50002L)
        .build();

    val testPerson = Person.builder()
        .lastName("DoeTest")
        .age(93)
        .male(true)
        .height(183L)
        .weight(203.0)
        .myChar('y')
        .numChildren(6)
        .hasHouse(false)
        .income(50003L)
        .build();

    val employee = Employee.builder()
        .person(person)
        .person2(person2)
        .testPerson(testPerson)
        .person3(null)
        .company("OICR")
        .build();

    val map = ENV_PROCESSOR.generateEnvMap(employee);
    val expectedEnvVariables = List.of(
        PERSON_LASTNAME,
        PERSON_AGE,
        PERSON_MALE,
        PERSON_HEIGHT,
        PERSON_WEIGHT,
        PERSON_MYCHAR,
        PERSON_NUM_CHILDREN,
        PERSON_HAS_HOUSE,
        PERSON_INCOME,

        PERSON2_LASTNAME,
        PERSON2_AGE,
        PERSON2_MALE,
        PERSON2_HEIGHT,
        PERSON2_WEIGHT,
        PERSON2_MYCHAR,
        PERSON2_NUM_CHILDREN,
        PERSON2_HAS_HOUSE,
        PERSON2_INCOME,

        TEST_PERSON2_LASTNAME,
        TEST_PERSON2_AGE,
        TEST_PERSON2_MALE,
        TEST_PERSON2_HEIGHT,
        TEST_PERSON2_WEIGHT,
        TEST_PERSON2_MYCHAR,
        TEST_PERSON2_NUM_CHILDREN,
        TEST_PERSON2_HAS_HOUSE,
        TEST_PERSON2_INCOME,

        COMPANY);
    val fieldsPerPerson = 9;
    val numPersonFieldsPerEmployee = 3;
    val numEmployeeFields = 1;
    val totalExpectedEnvVariables = numPersonFieldsPerEmployee*fieldsPerPerson + numEmployeeFields;
    assertEquals(totalExpectedEnvVariables, expectedEnvVariables.size());

    expectedEnvVariables
        .forEach(x -> Tester.assertTrue(map.containsKey(x),
            "Expected env variable '%s' does not exist in the generated map", x ));

    assertEquals(totalExpectedEnvVariables, map.size());

    assertEquals("Doe", map.get(PERSON_LASTNAME));
    assertEquals("Doe2", map.get(PERSON2_LASTNAME));
    assertEquals("DoeTest", map.get(TEST_PERSON2_LASTNAME));
    assertEquals("OICR", map.get(COMPANY));

  }

  @Test
  public void generateEnvMap_Person_Success(){
    val person= Person.builder()
        .lastName("Doe")
        .age(90)
        .male(true)
        .height(180L)
        .weight(200.0)
        .myChar('q')
        .numChildren(4)
        .hasHouse(true)
        .income(50000L)
        .build();

    val map = ENV_PROCESSOR.generateEnvMap(person);

    assertEquals(9, map.size());
    Stream.of(
        LASTNAME,
        AGE, MALE,
        HEIGHT,
        WEIGHT,
        MYCHAR,
        NUM_CHILDREN,
        HAS_HOUSE,
        INCOME )
        .forEach(x -> Tester.assertTrue(map.containsKey(x),
            "Expected env variable '%s' does not exist in the generated map", x ));

    assertEquals("Doe", map.get(LASTNAME));
    assertEquals("90", map.get(AGE));
    assertEquals("true", map.get(MALE));
    assertEquals("180", map.get(HEIGHT));
    assertEquals(Double.toString(200.0), map.get(WEIGHT));
    assertEquals("q", map.get(MYCHAR));
    assertEquals("4", map.get(NUM_CHILDREN));
    assertEquals("true", map.get(HAS_HOUSE));
    assertEquals("50000", map.get(INCOME));
  }

  @Test
  public void generateEnvMap_EnvCollisions_EnvProcessingException(){
    val exp1 = CollisionClass1.builder()
        .name("John")
        .nameClone("SomethingElse")
        .build();
    assertEnvProcessingException(exp1, "a collision was detected with the env variable" );

    val exp2 = CollisionClass2.builder()
        .name1("J1")
        .name2("J2")
        .build();
    assertEnvProcessingException(exp2, "a collision was detected with the env variable" );

    val exp3 = CollisionClass3.builder()
        .subClass1(CollisionClass3.MySubClass.builder().name("John").build())
        .subClass2(CollisionClass3.MySubClass.builder().name("Mike").build())
        .build();
    assertEnvProcessingException(exp3, "a collision was detected with the env variable" );
  }

  private static void assertEnvProcessingException(@Nullable Object obj, @NonNull String containingTextInMessage){
    assertExceptionThrown(() -> ENV_PROCESSOR.generateEnvMap(obj),
        EnvProcessingException.class,containingTextInMessage );
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Employee{

    private String company;

    private Person person;

    private Person person2;

    private Person person3;

    @EnvVariable("TEST_PERSON2")
    private Person testPerson;

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Person{

    private static final String NUM_CHILDREN = "NUM_CHILDREN";
    private static final String HAS_HOUSE = "HAS_HOUSE";

    private String lastName;

    private int age;

    private boolean male;

    private long height;

    private double weight;

    private char myChar;

    @EnvVariable(NUM_CHILDREN)
    private Integer numChildren;

    @EnvVariable(HAS_HOUSE)
    private Boolean hasHouse;

    private Long income;

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  private static class PrivateClass {
    @EnvVariable("TEST_PRIVATE_CLASS_NAME")
    private String name;

  }


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CollisionClass1 {

    private String name;

    @EnvVariable("NAME")
    private String nameClone;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CollisionClass2 {

    @EnvVariable("NAME")
    private String name1;

    @EnvVariable("NAME")
    private String name2;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CollisionClass3 {

    @EnvVariable("MY")
    private MySubClass subClass1;

    @EnvVariable("MY")
    private MySubClass subClass2;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MySubClass {
      private String name;
    }
  }


}
