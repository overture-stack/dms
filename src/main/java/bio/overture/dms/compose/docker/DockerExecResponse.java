package bio.overture.dms.compose.docker;

import java.io.OutputStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@Deprecated
public class DockerExecResponse {
  @NonNull private final OutputStream stdout;
  @NonNull private final OutputStream stderr;
}
