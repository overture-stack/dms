package bio.overture.dms.domain.compose;

import static bio.overture.dms.domain.compose.PullPolicies.IF_NOT_PRESENT;
import static bio.overture.dms.domain.compose.ServerRestartPolicies.NO;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import bio.overture.dms.model.compose.ComposeService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ComposeServer {

  @JsonIgnore private String name;

  private Deployment deploy;

  @JsonDeserialize(using = StringOrArrayDeserializer.class)
  private List<String> command = new ArrayList<>();

  private String containerName;

  @JsonDeserialize(using = DependsOnDeserializer.class)
  private List<Dependency> dependsOn = new ArrayList<>();

  private String domainname;

  @JsonDeserialize(using = StringOrArrayDeserializer.class)
  private List<String> entrypoint = new ArrayList<>();

  @JsonDeserialize(using = ComposeService.StringEqualsDCMapDeserializer.class)
  private Map<String, String> environment = new TreeMap<>();

  private List<Integer> expose = new ArrayList<>();

  private String hostname;

  private Healthcheck healthcheck;

  private String image;

  private String networkMode;

  @JsonDeserialize(using = ComposeService.IntegerColonDCMapDeserializer.class)
  private Map<Integer, Integer> ports = new HashMap<>();

  private PullPolicies pullPolicy = IF_NOT_PRESENT;
  private Boolean readOnly;
  private ServerRestartPolicies restart = NO;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long stopGracePeriod;

  private String stopSignal;
  private String user;

  // Note: dont support <src>:<target>:ro short format
  private List<ServerVolume> volumes = new ArrayList<>();
}
