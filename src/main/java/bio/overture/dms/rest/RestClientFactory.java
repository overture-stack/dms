package bio.overture.dms.rest;

import bio.overture.dms.rest.RestClient;
import lombok.NonNull;

public interface RestClientFactory {

  RestClient buildBearerAuthRestClient(@NonNull String bearerToken);

  RestClient buildNoAuthRestClient();
}
