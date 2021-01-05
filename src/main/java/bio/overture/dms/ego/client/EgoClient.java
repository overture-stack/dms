package bio.overture.dms.ego.client;

import bio.overture.dms.core.util.CollectionUtils;
import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.model.CreateApplicationRequest;
import bio.overture.dms.ego.model.EgoApplication;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.ego.model.ListApplicationRequest;
import bio.overture.dms.ego.model.PageDTO;
import bio.overture.dms.ego.model.UpdateApplicationRequest;
import bio.overture.dms.rest.RestClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Collectors;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

/** A api client for an externally running Ego service */
@RequiredArgsConstructor
public class EgoClient {

  /** Dependencies */
  @NonNull private final EgoEndpoint egoEndpoint;

  @NonNull private final ObjectSerializer jsonSerializer;
  @NonNull private final RestClient restClient;

  @SneakyThrows
  public EgoToken postAccessToken(@NonNull String clientId, @NonNull String clientSecret) {
    return restClient.post(
        egoEndpoint.postAccessToken(clientId, clientSecret),
        s -> jsonSerializer.convertValue(s, EgoToken.class));
  }

  @SneakyThrows
  public String getPublicKey() {
    return restClient.getString(egoEndpoint.getPublicKey());
  }

  public EgoApplication createApplication(@NonNull CreateApplicationRequest r) {
    return restClient.post(
        egoEndpoint.createApplication(),
        r,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public EgoApplication updateApplication(@NonNull String applicationId, @NonNull UpdateApplicationRequest r) {
    return restClient.put(
        egoEndpoint.updateApplication(applicationId),
        r,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public PageDTO<EgoApplication> listApplications(@NonNull ListApplicationRequest r){
    return restClient.get(egoEndpoint.listApplications(r), x -> deserializePage(x, EgoApplication.class));
  }

  @SuppressWarnings("unchecked")
  private <T> PageDTO<T> deserializePage(String body, Class<T> contentType){
    val erasedPageDTO = jsonSerializer.convertValue(body, PageDTO.class);
    val contents = mapToUnmodifiableList(erasedPageDTO.getResultSet(), x -> jsonSerializer.convertValue(x , contentType));
    return PageDTO.<T>builder()
        .count(erasedPageDTO.getCount())
        .limit(erasedPageDTO.getLimit())
        .offset(erasedPageDTO.getOffset())
        .resultSet(contents)
        .build();
  }

}
