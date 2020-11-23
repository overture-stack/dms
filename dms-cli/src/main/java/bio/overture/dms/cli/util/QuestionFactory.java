package bio.overture.dms.cli.util;

import bio.overture.dms.cli.model.question.OptionalQuestion;
import bio.overture.dms.cli.model.question.RequiredQuestion;
import bio.overture.dms.cli.model.question.impl.MCQuestionDecorator;
import bio.overture.dms.cli.model.question.impl.OneHotQuestionDecorator;
import bio.overture.dms.cli.model.question.impl.OptionalQuestionDecorator;
import bio.overture.dms.cli.model.question.impl.RequiredQuestionImpl;

import java.util.Set;

public class QuestionFactory {


  public static <T> RequiredQuestionImpl<T> buildSimpleRequiredQuestion(String question){
    return new RequiredQuestionImpl<>(question);
  }

  public static <T> OptionalQuestion<T>  buildSimpleOptionalQuestion(String question,T defaultAnswer){
    return OptionalQuestionDecorator.<T>builder()
        .internalQuestion(buildSimpleRequiredQuestion(question))
        .defaultAnswer(defaultAnswer)
        .build();
  }

  public static <T> RequiredQuestion<Set<T>> buildMCRequiredQuestion(String question, Set<T> selections){
    return MCQuestionDecorator.<T>builder()
        .internalQuestion(buildSimpleRequiredQuestion(question))
        .selections(selections)
        .build();
  }

  public static <T> OptionalQuestion<Set<T>> buildMCOptionalQuestion(String question, Set<T> selections, Set<T> defaultSelections){
    return OptionalQuestionDecorator.<Set<T>>builder()
        .internalQuestion(buildMCRequiredQuestion(question, selections))
        .defaultAnswer(defaultSelections)
        .build();
  }

  public static <T> RequiredQuestion<T> buildOneHotRequiredQuestion(String question, Set<T> selections){
    return OneHotQuestionDecorator.<T>builder()
        .internalQuestion(buildSimpleRequiredQuestion(question))
        .selections(selections)
        .build();
  }

  public static <T> OptionalQuestion<T> buildOneHotOptionalQuestion(String question, Set<T> selections, T defaultSelection){
    return OptionalQuestionDecorator.<T>builder()
        .internalQuestion(buildOneHotRequiredQuestion(question, selections))
        .defaultAnswer(defaultSelection)
        .build();
  }

}
