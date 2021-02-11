package bio.overture.dms.core.model.dmsconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class MaestroConfig {
  public static final int DEFAULT_PORT = 11235;
  @Builder.Default private int hostPort = DEFAULT_PORT;
}


