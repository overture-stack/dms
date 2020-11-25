package bio.overture.dms.docker;

import java.io.OutputStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DockerExecResponse {
  @NonNull private final OutputStream stdout;
  @NonNull private final OutputStream stderr;
}
