package bio.overture.dms.rest;

import lombok.NonNull;

public interface RestClientFactory {

  RestClient buildBearerAuthRestClient(@NonNull String bearerToken);

  RestClient buildNoAuthRestClient();
}
