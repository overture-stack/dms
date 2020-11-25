package bio.overture.dms.cli.question;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.beryx.textio.InputReader;

@Getter
@RequiredArgsConstructor
public abstract class AbstractQuestion<T, A> {

  @NonNull private final String question;
  @NonNull private final InputReader<T, ?> inputReader;

  public abstract A getAnswer();

}
