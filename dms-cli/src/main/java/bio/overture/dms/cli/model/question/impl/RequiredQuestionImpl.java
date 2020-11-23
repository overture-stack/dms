package bio.overture.dms.cli.model.question.impl;

import bio.overture.dms.cli.model.question.RequiredQuestion;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RequiredQuestionImpl<T> implements RequiredQuestion<T> {

  /**
   * Required
   */
  private final String question;

  /**
   * Stateful
   */
  private T answer;

  @Builder
  public RequiredQuestionImpl(@NonNull String question) {
    this.question = question;
  }

}
