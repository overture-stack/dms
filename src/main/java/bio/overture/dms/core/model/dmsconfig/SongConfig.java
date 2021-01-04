package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.core.util.Strings.isDefined;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class SongConfig {

  @Min(value = 2000)
  @Builder.Default
  private int apiHostPort = 9010;

  @NotNull private URL serverUrl;

  @Pattern(regexp = "^[A-Za-z0-9]+")
  private String databasePassword;

  @Min(value = 2000)
  @Builder.Default
  private int dbHostPort = 9011;

  @JsonIgnore
  public boolean isDatabasePasswordDefined() {
    return isDefined(databasePassword);
  }
}
