package bio.overture.dms.cli.question;

import org.beryx.textio.InputReader;

import java.util.List;

public class SelectionQuestion<T> extends AbstractQuestion<T, List<T>> {

  public SelectionQuestion(String question, InputReader<T, ?> inputReader) {
    super(question, inputReader);
  }

  @Override
  public List<T> getAnswer() {
    return getInputReader().readList(getQuestion());
  }
}
