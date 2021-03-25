package bio.overture.dms.cli.question;

import static bio.overture.dms.cli.model.enums.QuestionProfiles.QUESTION;
import static bio.overture.dms.core.model.enums.FieldTypes.isBoolean;
import static bio.overture.dms.core.model.enums.FieldTypes.isEnum;
import static bio.overture.dms.core.model.enums.FieldTypes.isInteger;
import static bio.overture.dms.core.model.enums.FieldTypes.isLong;
import static bio.overture.dms.core.model.enums.FieldTypes.isString;
import static bio.overture.dms.core.model.enums.FieldTypes.isUrl;
import static bio.overture.dms.core.util.Exceptions.checkArgument;
import static bio.overture.dms.core.util.Joiner.COMMA;
import static bio.overture.dms.core.util.Strings.isNotDefined;
import static java.util.Objects.isNull;

import bio.overture.dms.cli.model.enums.QuestionProfiles;
import bio.overture.dms.cli.question.validation.EmailValidator;
import bio.overture.dms.cli.question.validation.FileValidator;
import bio.overture.dms.cli.question.validation.QuestionValidator;
import bio.overture.dms.cli.question.validation.UrlQuestionValidator;
import bio.overture.dms.cli.terminal.UrlInputReader;
import bio.overture.dms.core.util.Nullable;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.beryx.textio.InputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Note: Since the QuestionFactory depends on TextTerminal, in a test context it does not make sense
 * to share a TestTextTerminal, since multiple tests would be mutating a singleton TextTerminal
 * instance. QuestionFactory should be instantiated once per test, along with its TestTextTerminal.
 */
@Slf4j
@Component
public class QuestionFactory {

  /** Constants */
  private static final QuestionProfiles DEFAULT_QUESTION_PROFILE = QUESTION;

  private static final UrlQuestionValidator URL_QUESTION_VALIDATOR = new UrlQuestionValidator();

  private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

  private static final FileValidator FILE_VALIDATOR = new FileValidator();

  /** Dependencies */
  private final TextIO textIO;

  @Autowired
  public QuestionFactory(@NonNull TextIO textIO) {
    this.textIO = textIO;
  }

  public <T> SingleQuestion<T> newSingleQuestion(
      @NonNull QuestionProfiles profile,
      @NonNull Class<T> answerType,
      @NonNull String question,
      boolean optional,
      T defaultValue) {
    return new SingleQuestion<T>(
        question, buildSingleInputReader(profile, answerType, optional, defaultValue));
  }

  public <T> SingleQuestion<T> newDefaultSingleQuestion(
      @NonNull Class<T> answerType, @NonNull String question, boolean optional, T defaultValue) {
    return new SingleQuestion<T>(
        question, buildSingleInputReader(null, answerType, optional, defaultValue));
  }

  public <T> SingleQuestion<T> newDefaultSingleQuestion(
      @NonNull Class<T> answerType,
      @NonNull String question,
      boolean optional,
      T defaultValue,
      @NonNull QuestionValidator<T> questionValidator) {
    return new SingleQuestion<T>(
        question,
        buildSingleInputReader(null, answerType, optional, defaultValue)
            .withValueChecker(questionValidator.createValueChecker()));
  }

  public SingleQuestion<URL> newUrlSingleQuestion(
      @NonNull String question, boolean optional, URL defaultValue) {
    return newDefaultSingleQuestion(
        URL.class, question, optional, defaultValue, URL_QUESTION_VALIDATOR);
  }

  public SingleQuestion<String> newEmailQuestion(
      @NonNull String question, boolean optional, String defaultVal) {
    return newDefaultSingleQuestion(String.class, question, optional, defaultVal, EMAIL_VALIDATOR);
  }

  public SingleQuestion<String> newFileQuestion(@NonNull String question, boolean optional) {
    return newDefaultSingleQuestion(String.class, question, optional, null, FILE_VALIDATOR);
  }

  public SingleQuestion<String> newPasswordQuestion(@NonNull String question) {
    return new SingleQuestion<>(question, buildPasswordInputReader());
  }

  /** An MC Question is one that has multiple options and multiple selections can be made */
  public <T> SelectionQuestion<T> newMCQuestion(
      @NonNull Class<T> answerType,
      @NonNull String question,
      @NonNull List<T> selection,
      boolean optional,
      T defaultValue) {
    return new SelectionQuestion<T>(
        question, buildSimpleSelectionInputReader(answerType, selection, optional, defaultValue));
  }

  public <E extends Enum<E>> SelectionQuestion<E> newMCQuestion(
      @NonNull Class<E> answerType, @NonNull String question, boolean optional, E defaultValue) {
    return new SelectionQuestion<E>(
        question, buildEnumSelectionInputReader(answerType, optional, defaultValue));
  }

  /** An OneHot Question is one that has multiple options and only ONE selection can be made */
  public <T> SingleQuestion<T> newOneHotQuestion(
      @NonNull Class<T> answerType,
      @NonNull String question,
      @NonNull List<T> selection,
      boolean optional,
      T defaultValue) {
    return new SingleQuestion<T>(
        question, buildSimpleSelectionInputReader(answerType, selection, optional, defaultValue));
  }

  public <E extends Enum<E>> SingleQuestion<E> newOneHotQuestion(
      @NonNull Class<E> answerType, @NonNull String question, boolean optional, E defaultValue) {
    return new SingleQuestion<E>(
        question, buildEnumSelectionInputReader(answerType, optional, defaultValue));
  }

  private <T> InputReader<T, ?> buildCommonInputReader(
      QuestionProfiles profile, @NonNull Class<T> answerType, boolean optional, T defaultValue) {
    val resolvedProfile = resolveProfile(profile);
    var ir =
        buildDefaultInputReader(textIO, answerType)
            .withPromptAdjustments(true)
            .withPropertiesPrefix(resolvedProfile)
            .withInputTrimming(true);
    if (optional) {
      checkArgument(!isNull(defaultValue), "The defaultValue cannot be null when optional is true");
      ir = ir.withDefaultValue(defaultValue);
    }
    return ir;
  }

  private <T> InputReader<T, ?> buildSingleInputReader(
      @Nullable QuestionProfiles profile,
      @NonNull Class<T> answerType,
      boolean optional,
      T defaultValue) {
    return buildCommonInputReader(profile, answerType, optional, defaultValue);
  }

  private InputReader<String, ?> buildPasswordInputReader() {
    return buildSingleInputReader(null, String.class, false, null).withInputMasking(true);
  }

  //  @SuppressWarnings("unchecked")
  private <T extends Enum<T>> InputReader<T, ?> buildEnumSelectionInputReader(
      @NonNull Class<T> answerType, boolean optional, T defaultValue) {
    checkArgument(
        !optional || !isNull(defaultValue),
        "Default value cannot be null when optional mode is used");
    var ir = buildCommonInputReader(null, answerType, optional, defaultValue);
    if (optional) {
      ir =
          ir.withValueListChecker(
              (values, itemName) -> {
                boolean hasBlankValue;
                if (values != null && !values.isEmpty()) {
                  hasBlankValue = values.stream().anyMatch(Objects::isNull);
                } else {
                  hasBlankValue = true;
                }

                if (hasBlankValue) {
                  return List.of("Empty responses not allowed");
                }
                return null;
              });
    }
    return ir;
  }

  private <T> InputReader<T, ?> buildSimpleSelectionInputReader(
      @NonNull Class<T> answerType, @NonNull List<T> selections, boolean optional, T defaultValue) {
    checkArgument(!selections.isEmpty(), "Must define atleast 1 selection");
    checkArgument(
        !optional || !isNull(defaultValue),
        "Default value cannot be null when optional mode is used");
    checkArgument(
        !optional || selections.contains(defaultValue),
        "The selection [%s] does not contain the defaultValue '%s'",
        COMMA.join(selections),
        defaultValue);

    var ir =
        buildCommonInputReader(null, answerType, optional, defaultValue)
            .withNumberedPossibleValues(selections);
    if (optional) {
      ir =
          ir.withValueListChecker(
              (values, itemName) -> {
                boolean hasBlankValue;
                if (values != null && !values.isEmpty()) {
                  hasBlankValue =
                      values.stream()
                          .anyMatch(
                              x -> {
                                if (x == null) {
                                  return true;
                                } else if (x instanceof String) {
                                  val stringVal = (String) x;
                                  return isNotDefined(stringVal);
                                }
                                return false;
                              });
                } else {
                  hasBlankValue = true;
                }

                if (hasBlankValue) {
                  return List.of("Empty responses not allowed");
                }
                return null;
              });
    }
    return ir;
  }

  public static QuestionFactory buildQuestionFactory(@NonNull TextTerminal<?> textTerminal) {
    return new QuestionFactory(new TextIO(textTerminal));
  }

  private static String resolveProfile(@Nullable QuestionProfiles profile) {
    if (isNull(profile)) {
      log.debug("Profile not defined, using default profile '{}'", DEFAULT_QUESTION_PROFILE);
      return DEFAULT_QUESTION_PROFILE.toString();
    }
    return profile.toString();
  }

  @SuppressWarnings("unchecked")
  private static <T> InputReader<T, ?> buildDefaultInputReader(TextIO t, Class<T> answerType) {
    if (isBoolean(answerType)) {
      return (InputReader<T, ?>) t.newBooleanInputReader();
    } else if (isLong(answerType)) {
      return (InputReader<T, ?>) t.newLongInputReader();
    } else if (isInteger(answerType)) {
      return (InputReader<T, ?>) t.newIntInputReader();
    } else if (isString(answerType)) {
      return (InputReader<T, ?>) t.newStringInputReader();
    } else if (isEnum(answerType)) {
      val a = (Class<Enum>) answerType;
      return (InputReader<T, ?>) t.newEnumInputReader(a);
    } else if (isUrl(answerType)) {
      return (InputReader<T, ?>) new UrlInputReader(t::getTextTerminal);
    } else {
      throw new IllegalArgumentException(
          "Could not build converter for answer of type: " + answerType.getCanonicalName());
    }
  }
}
