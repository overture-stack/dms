package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvObject;
import bio.overture.dms.core.properties.env.EnvProcessor;
import bio.overture.dms.core.properties.env.EnvVariable;
import bio.overture.dms.core.properties.exceptions.EnvProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.File;
import java.util.HashMap;
import java.util.stream.Stream;

import static bio.overture.dms.core.properties.env.EnvFieldProcessor.createFieldProcessor;
import static bio.overture.dms.core.properties.env.EnvVars.generateEnvVarMap;
import static bio.overture.dms.core.util.Tester.assertExceptionThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class EnvTest {
  private static final String TEST_LASTNAME = "TEST_LASTNAME";
  private static final String TEST_AGE = "TEST_AGE";
  private static final String TEST_IS_MALE = "TEST_IS_MALE";
  private static final String TEST_HEIGHT= "TEST_HEIGHT";
  private static final String TEST_WEIGHT = "WEIGHT";
  private static final String TEST_MY_CHAR = "TEST_MY_CHAR";
  private static final String TEST_NUM_CHILDREN = "TEST_NUM_CHILDREN";
  private static final String TEST_HAS_HOUSE= "TEST_HAS_HOUSE";
  private static final String TEST_INCOME = "TEST_INCOME";
  private static final String TEST_COMPANY =  "TEST_COMPANY";

  private static final EnvProcessor ENV_PROCESSOR = Factory.buildEnvProcessor();

  @Test
  public void renderVariables_Private_EnvProcessingException(){
    assertExceptionThrown(() -> generateEnvVarMap(new PrivateClass()), EnvProcessingException.class);
  }

  @Test
  public void renderVariables_Employee_Success(){
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
        .company("OICR")
        .build();

    val map = createFieldProcessor(Employee.class).generateEnvVarMap(employee);

//    val r = new Reflections(getClass().getPackageName(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new MemberUsageScanner());
//    r.save("hi.xml");
//
//    val r2 = new Reflections().collect(new File("hi.xml"));
//
    val proc = Factory.buildEnvProcessor();
    val newmap = proc.generateEnvMap(employee);
    log.info("sdf");

  }

  // TODO: test undefined (null) none custom object
  // TODO: test undefined (null) custom object
  @Test
  public void renderVariables_Person_Success(){
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


    val map = createFieldProcessor(Person.class).generateEnvVarMap(person);


    assertEquals(9, map.size());
    Stream.of(
        TEST_LASTNAME,
        TEST_AGE,
        TEST_IS_MALE,
        TEST_HEIGHT,
        TEST_WEIGHT,
        TEST_MY_CHAR,
        TEST_NUM_CHILDREN,
        TEST_HAS_HOUSE,
        TEST_INCOME )
        .forEach(x -> assertTrue(map.containsKey(x)));

    assertEquals("Doe", map.get(TEST_LASTNAME));
    assertEquals("90", map.get(TEST_AGE));
    assertEquals("true", map.get(TEST_IS_MALE));
    assertEquals("180", map.get(TEST_HEIGHT));
    assertEquals(Double.toString(200.0), map.get(TEST_WEIGHT));
    assertEquals("q", map.get(TEST_MY_CHAR));
    assertEquals("4", map.get(TEST_NUM_CHILDREN));
    assertEquals("true", map.get(TEST_HAS_HOUSE));
    assertEquals("50000", map.get(TEST_INCOME));
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


}
