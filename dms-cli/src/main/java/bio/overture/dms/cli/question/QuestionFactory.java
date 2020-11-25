package bio.overture.dms.cli.question;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.beryx.textio.InputReader;
import org.beryx.textio.TextIO;

import java.util.List;
import java.util.Objects;

import static bio.overture.dms.core.enums.FieldTypes.isBoolean;
import static bio.overture.dms.core.enums.FieldTypes.isEnum;
import static bio.overture.dms.core.enums.FieldTypes.isInteger;
import static bio.overture.dms.core.enums.FieldTypes.isLong;
import static bio.overture.dms.core.enums.FieldTypes.isString;
import static bio.overture.dms.core.util.Exceptions.checkArgument;
import static bio.overture.dms.core.util.Joiner.COMMA;
import static bio.overture.dms.core.util.Strings.isBlank;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class QuestionFactory {

  @NonNull private final TextIO textIO;

  public <T> SingleQuestion<T> newSingleQuestion(@NonNull Class<T> answerType, @NonNull String question, boolean optional, T defaultValue){
    return new SingleQuestion<T>(question, buildSingleInputReader(answerType, optional, defaultValue) );
  }

  public SingleQuestion<String> newPasswordQuestion(@NonNull String question){
    return new SingleQuestion<>(question, buildPasswordInputReader() );
  }

  public <T> SelectionQuestion<T> newMCQuestion(@NonNull Class<T> answerType, @NonNull String question, @NonNull List<T> selection, boolean optional, T defaultValue){
    return new SelectionQuestion<T>(question, buildSimpleSelectionInputReader(answerType, selection, optional, defaultValue));
  }

  public <E extends Enum<E>> SelectionQuestion<E> newMCQuestion(@NonNull Class<E> answerType, @NonNull String question, boolean optional, E defaultValue){
    return new SelectionQuestion<E>(question, buildEnumSelectionInputReader(answerType, optional, defaultValue));
  }

  public <T> SingleQuestion<T> newOneHotQuestion(@NonNull Class<T> answerType, @NonNull String question, @NonNull List<T> selection, boolean optional, T defaultValue){
    return new SingleQuestion<T>(question, buildSimpleSelectionInputReader(answerType, selection, optional, defaultValue));
  }

  public <E extends Enum<E>> SingleQuestion<E> newOneHotQuestion(@NonNull Class<E> answerType, @NonNull String question, boolean optional, E defaultValue){
    return new SingleQuestion<E>(question, buildEnumSelectionInputReader(answerType, optional, defaultValue));
  }

  private <T> InputReader<T,?> buildCommonInputReader(@NonNull Class<T> answerType, boolean optional, T defaultValue){
    var ir = buildDefaultInputReader(textIO, answerType)
        .withPromptAdjustments(true)
        .withPropertiesPrefix("question")
        .withInputTrimming(true);
    if (optional){
      checkArgument(!isNull(defaultValue), "The defaultValue cannot be null when optional is true");
      ir = ir.withDefaultValue(defaultValue);
    }
    return ir;
  }

  private <T> InputReader<T,?> buildSingleInputReader(@NonNull Class<T> answerType, boolean optional, T defaultValue){
    return buildCommonInputReader(answerType, optional, defaultValue);
  }

  private InputReader<String,?> buildPasswordInputReader(){
    return buildSingleInputReader(String.class, false, null)
        .withInputMasking(true);
  }

//  @SuppressWarnings("unchecked")
  private <T extends Enum<T>> InputReader<T,?> buildEnumSelectionInputReader(@NonNull Class<T> answerType, boolean optional, T defaultValue){
    checkArgument(!optional || !isNull(defaultValue), "Default value cannot be null when optional mode is used");
    var ir = buildCommonInputReader(answerType, optional, defaultValue);
    if (optional){
      ir = ir.withValueListChecker((values, itemName) -> {
        boolean hasBlankValue;
        if (values != null  && !values.isEmpty()){
          hasBlankValue = values.stream().anyMatch(Objects::isNull);
        } else {
          hasBlankValue = true;
        }

        if (hasBlankValue){
          return List.of("Empty responses not allowed");
            }
            return null;
          }
      );

    }
    return ir;
  }

  private <T> InputReader<T,?> buildSimpleSelectionInputReader(@NonNull Class<T> answerType, @NonNull List<T> selections, boolean optional, T defaultValue){
    checkArgument(!selections.isEmpty(), "Must define atleast 1 selection");
    checkArgument(!optional || !isNull(defaultValue), "Default value cannot be null when optional mode is used");
    checkArgument( !optional || selections.contains(defaultValue),
        "The selection [%s] does not contain the defaultValue '%s'",
        COMMA.join(selections), defaultValue);

    var ir = buildCommonInputReader(answerType,optional, defaultValue)
        .withNumberedPossibleValues(selections);
    if (optional){
      ir = ir.withValueListChecker((values, itemName) -> {
        boolean hasBlankValue;
        if (values != null  && !values.isEmpty()){
          hasBlankValue = values.stream()
              .anyMatch(x -> {
                if (x == null){
                  return true;
                } else if (x instanceof String){
                  val stringVal = (String)x;
                  return isBlank(stringVal);
                }
                return false;
              });
        } else {
          hasBlankValue = true;
        }

        if (hasBlankValue){
          return List.of("Empty responses not allowed");
        }
        return null;
      }
      );

    }
    return ir;
  }

  @SuppressWarnings("unchecked")
  private static <T> InputReader<T,?>  buildDefaultInputReader(TextIO t, Class<T> answerType){
    if (isBoolean(answerType)){
      return (InputReader<T,?>)t.newBooleanInputReader();
    } else if (isLong(answerType)){
      return (InputReader<T,?>)t.newLongInputReader();
    } else if (isInteger(answerType)){
      return (InputReader<T,?>)t.newIntInputReader();
    } else if (isString(answerType)){
      return (InputReader<T,?>)t.newStringInputReader();
    } else if(isEnum(answerType)){
      val a = (Class<Enum>)answerType;
      return (InputReader<T, ?>) t.newEnumInputReader(a);
    } else {
      throw new IllegalArgumentException("Could not build converter for answer of type: "+answerType.getCanonicalName());
    }
  }


}
