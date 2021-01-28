package bio.overture.dms.ego.client;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableList;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.model.ApplicationPermission;
import bio.overture.dms.ego.model.ApplicationRequest;
import bio.overture.dms.ego.model.EgoApplication;
import bio.overture.dms.ego.model.EgoGroup;
import bio.overture.dms.ego.model.EgoPolicy;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.ego.model.GroupRequest;
import bio.overture.dms.ego.model.ListApplicationPermissionsRequest;
import bio.overture.dms.ego.model.ListApplicationRequest;
import bio.overture.dms.ego.model.ListGroupPermissionsRequest;
import bio.overture.dms.ego.model.ListGroupRequest;
import bio.overture.dms.ego.model.ListPolicyRequest;
import bio.overture.dms.ego.model.PageDTO;
import bio.overture.dms.ego.model.PermissionRequest;
import bio.overture.dms.ego.model.PolicyRequest;
import bio.overture.dms.ego.model.PolicyResponse;
import bio.overture.dms.rest.RestClient;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

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

  public EgoPolicy createPolicy(@NonNull PolicyRequest r) {
    return restClient.post(
        egoEndpoint.createPolicy(), r, x -> jsonSerializer.convertValue(x, EgoPolicy.class));
  }

  public EgoPolicy updatePolicy(@NonNull String policyId, @NonNull PolicyRequest r) {
    return restClient.put(
        egoEndpoint.updatePolicy(policyId),
        r,
        x -> jsonSerializer.convertValue(x, EgoPolicy.class));
  }

  public EgoApplication createApplication(@NonNull ApplicationRequest r) {
    return restClient.post(
        egoEndpoint.createApplication(),
        r,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public EgoApplication createApplicationPermission(
      @NonNull String applicationId, @NonNull Collection<PermissionRequest> permissionRequests) {
    return restClient.post(
        egoEndpoint.createApplicationPermission(applicationId),
        permissionRequests,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public PageDTO<ApplicationPermission> listApplicationPermissions(
      @NonNull ListApplicationPermissionsRequest request) {
    return restClient.get(
        egoEndpoint.listApplicationPermission(request),
        x -> deserializePage(x, ApplicationPermission.class));
  }

  public EgoApplication updateApplication(
      @NonNull String applicationId, @NonNull ApplicationRequest r) {
    return restClient.put(
        egoEndpoint.updateApplication(applicationId),
        r,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public PageDTO<EgoApplication> listApplications(@NonNull ListApplicationRequest r) {
    return restClient.get(
        egoEndpoint.listApplications(r), x -> deserializePage(x, EgoApplication.class));
  }

  public PageDTO<EgoGroup> listGroups(@NonNull ListGroupRequest r) {
    return restClient.get(egoEndpoint.listGroups(r), x -> deserializePage(x, EgoGroup.class));
  }

  public PageDTO<EgoPolicy> listPolicies(@NonNull ListPolicyRequest r) {
    return restClient.get(egoEndpoint.listPolicies(r), x -> deserializePage(x, EgoPolicy.class));
  }

  public PageDTO<PolicyResponse> listGroupPermissions(@NonNull ListGroupPermissionsRequest r) {
    return restClient.get(
        egoEndpoint.listGroupPermissions(r), x -> deserializePage(x, PolicyResponse.class));
  }

  public EgoGroup createGroup(@NonNull GroupRequest r) {
    return restClient.post(
        egoEndpoint.createGroup(), r, x -> jsonSerializer.convertValue(x, EgoGroup.class));
  }

  public EgoGroup updateGroup(String groupId, GroupRequest r) {
    return restClient.put(
        egoEndpoint.updateGroup(groupId), r, x -> jsonSerializer.convertValue(x, EgoGroup.class));
  }

  public EgoGroup createGroupPermission(
      @NonNull String groupId, @NonNull Collection<PermissionRequest> permissionRequests) {
    return restClient.post(
        egoEndpoint.createGroupPermission(groupId),
        permissionRequests,
        x -> jsonSerializer.convertValue(x, EgoGroup.class));
  }

  @SuppressWarnings("unchecked")
  private <T> PageDTO<T> deserializePage(String body, Class<T> contentType) {
    val erasedPageDTO = jsonSerializer.convertValue(body, PageDTO.class);
    val contents =
        mapToUnmodifiableList(
            erasedPageDTO.getResultSet(), x -> jsonSerializer.convertValue(x, contentType));
    return PageDTO.<T>builder()
        .count(erasedPageDTO.getCount())
        .limit(erasedPageDTO.getLimit())
        .offset(erasedPageDTO.getOffset())
        .resultSet(contents)
        .build();
  }
}
