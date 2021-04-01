package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.cli.model.Constants.DockerImagesConstants.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class DmsUIConfig {

  @Min(value = 2000)
  @Builder.Default
  private int hostPort = 8000;

  @NonNull URL url;

  @NonNull @Email private String adminEmail;

  private String ssoProviders;

  @NonNull @Builder.Default private String labName = "Data Management System";

  private String logoFileName;

  private String assetsDir;

  private ArrangerProjectConfig projectConfig;

  public static final int DEFAULT_PORT = 8000;

  @JsonIgnore
  private String image =  OVERTURE_DMS_UI + ":" + DMS_UI_TAG;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ArrangerProjectConfig {
    public static final String DEFAULT_PROJECT_ID = "file";
    public static final String DEFAULT_PROJECT_NAME = "file";
    public static final String DEFAULT_INDEX_ALIAS = "file_centric";
    @NonNull @Builder.Default private String id = DEFAULT_PROJECT_ID;
    @NonNull @Builder.Default private String name = DEFAULT_PROJECT_NAME;
    @NonNull @Builder.Default private String indexAlias = DEFAULT_INDEX_ALIAS;
  }
}
