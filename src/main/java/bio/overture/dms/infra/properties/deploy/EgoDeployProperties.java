package bio.overture.dms.infra.properties.deploy;

import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.properties.service.PostgresServiceProperties;
import bio.overture.dms.infra.properties.service.ego.EgoApiServiceProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class EgoDeployProperties {

  @NonNull private final DockerContainer<EgoApiServiceProperties> egoApiDockerContainer;
  @NonNull private final DockerContainer<PostgresServiceProperties> egoDbDockerContainer;
}
