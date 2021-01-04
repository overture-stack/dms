package bio.overture.dms.util;

import static bio.overture.dms.core.util.Joiner.NONE;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.beryx.textio.AbstractTextTerminal;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@RequiredArgsConstructor(access = PRIVATE)
public class TestTextTerminal extends AbstractTextTerminal<TestTextTerminal> {
  public static final int DEFAULT_MAX_READS = 100;

  private int maxReads = DEFAULT_MAX_READS;
  private final List<String> inputs = new ArrayList<>();
  private int inputIndex = -1;
  private final List<String> outputs = new ArrayList<>();

  @Override
  public String read(boolean masking) {
    if (inputs.isEmpty())
      throw new IllegalStateException("No entries available in the 'inputs' list");
    inputIndex++;
    if (inputIndex >= maxReads) throw new RuntimeException("Too many read calls");
    val value = inputs.get((inputIndex < inputs.size()) ? inputIndex : -1);
    appendLn(value);
    return value;
  }

  @Override
  public void rawPrint(String message) {
    append(message);
  }

  @Override
  public void println() {
    append(lineSeparator());
  }

  public void reset() {
    inputs.clear();
    outputs.clear();
    inputIndex = -1;
  }

  public TestTextTerminal addInput(String input) {
    getInputs().add(input);
    return this;
  }

  public TestTextTerminal addInputs(String... inputStrings) {
    stream(inputStrings).forEach(this::addInput);
    return this;
  }

  public List<String> getInputs() {
    return inputs;
  }

  public List<String> getOutputLines(boolean ignoreReset) {
    val out = List.copyOf(outputs);
    if (!ignoreReset){
      reset();
    }
    return out;
  }

  /**
   * In the case that a single TestTextTerminal instance is a shared component in more than one test,
   * the terminal output is automatically reset/erased once the outputs are fetched.
   * This is to prevent the context of a previous test from interfering with the current test
   * This is NOT thread safe, so test must be run synchronously.
   * @param strip
   * @return
   */
  public String getOutputAndReset(boolean strip) {
    return getOutput(strip, false);
  }

  public String getOutput(boolean strip, boolean ignoreReset) {
    val out = NONE.join(getOutputLines(ignoreReset));
    if (strip) {
      return stripAll(out);
    }
    return out;
  }

  public int getReadCalls() {
    return inputIndex + 1;
  }

  public int getMaxReads() {
    return maxReads;
  }

  public void setMaxReads(int maxReads) {
    this.maxReads = maxReads;
  }

  private TestTextTerminal append(String s) {
    outputs.add(s);
    return this;
  }

  private TestTextTerminal appendLn(String s) {
    return append(s).append(lineSeparator());
  }

  public static String stripAll(String text) {
    if (text == null) return null;
    return stream(text.split("\\R"))
        .map(s -> s.replaceAll("\\t", ""))
        .map(s -> s.replaceAll("^\\s+|\\s+$", ""))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining("\n"));
  }

  public static TestTextTerminal createTestTextTerminal() {
    return new TestTextTerminal();
  }
}
