package bio.overture.dms.ego.client;

import bio.overture.dms.ego.model.CreateApplicationRequest;
import bio.overture.dms.ego.model.EgoApplication;
import bio.overture.dms.ego.model.ListApplicationRequest;
import bio.overture.dms.ego.model.UpdateApplicationRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class EgoService {

  @NonNull private final EgoClient client;

  /**
   * Create or Update an existing application. This should be idempotent
   */
  public EgoApplication saveApplication(@NonNull CreateApplicationRequest r) {
    val result = findApplicationByName(r.getName());
    if (result.isPresent()){
      val appId = result.get().getId();
      val updateRequest = resolveUpdateRequest(r);
      return client.updateApplication(appId, updateRequest);
    } else {
      return client.createApplication(r);
    }
  }

  public Optional<EgoApplication> findApplicationByName(@NonNull String applicationName) {
    val listAppRequest = new ListApplicationRequest();
    listAppRequest.setOffset(0);
    listAppRequest.setLimit(30);
    listAppRequest.setQuery(applicationName);

    while(true){
      val page = client.listApplications(listAppRequest);
      if (page.getResultSet().isEmpty()){
        return Optional.empty();
      }
      val result = page.getResultSet().stream()
          .filter(x -> x.getName().equals(applicationName))
          .findFirst();
      if (result.isPresent()){
        return result;
      } else {
        listAppRequest.setOffset(listAppRequest.getOffset()+listAppRequest.getLimit());
      }
    }
  }

  private static UpdateApplicationRequest resolveUpdateRequest(CreateApplicationRequest r){
    return UpdateApplicationRequest.builder()
        .clientId(r.getClientId())
        .clientSecret(r.getClientSecret())
        .description(r.getDescription())
        .name(r.getName())
        .redirectUri(r.getRedirectUri())
        .status(r.getStatus())
        .type(r.getType())
        .build();
  }

}
