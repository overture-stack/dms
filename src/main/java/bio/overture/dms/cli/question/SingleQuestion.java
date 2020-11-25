package bio.overture.dms.cli.question;

import org.beryx.textio.InputReader;

public class SingleQuestion<T> extends AbstractQuestion<T, T> {

  public SingleQuestion(String question, InputReader<T, ?> inputReader) {
    super(question, inputReader);
  }

  @Override
  public T getAnswer() {
    return getInputReader().read(getQuestion());
  }
}
