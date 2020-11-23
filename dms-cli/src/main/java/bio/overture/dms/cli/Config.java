package bio.overture.dms.cli;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Config {

  @QuestionToAsk(value = "What is your firstname?")
  private String firstName;

  @QuestionToAsk(
      value = "What is your lastname?",
      dependentFieldNames = {Fields.firstName})
  private String lastName;

  @QuestionToAsk(
      value = "What is your age?",
      dependentFieldNames = {Fields.lastName})
  private int age;
}
