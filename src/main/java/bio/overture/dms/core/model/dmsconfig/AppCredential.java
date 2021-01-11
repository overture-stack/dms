package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.core.util.Strings.isDefined;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the ego application credentials, that is used by this program to call any endpoint on
 * the ego service. This implies, the app credential is of role ADMIN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class AppCredential {

  private String name;
  private String clientId;
  private String clientSecret;
  private String redirectUri;

  @JsonIgnore
  public boolean isSecretDefined() {
    return isDefined(clientSecret);
  }
}
