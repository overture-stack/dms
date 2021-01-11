package bio.overture.dms.compose.model.stack;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DependencyTypes {
  @JsonProperty("service_started")
  SERVICE_STARTED,

  @JsonProperty("service_healthy")
  SERVICE_HEALTHY;
}
