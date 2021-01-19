package bio.overture.dms.ego.client;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import bio.overture.dms.ego.model.ListApplicationRequest;
import bio.overture.dms.ego.model.ListGroupPermissionsRequest;
import bio.overture.dms.ego.model.ListGroupRequest;
import bio.overture.dms.ego.model.ListPolicyRequest;
import bio.overture.dms.rest.params.RequestParamBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/** Resolves the endpoints for a specific api function */
@RequiredArgsConstructor
public class EgoEndpoint {

  private static final String OFFSET = "offset";
  private static final String LIMIT = "limit";
  private static final String QUERY = "query";

  @NonNull private final String baseServerUrl;

  public String postAccessToken(@NonNull String clientId, @NonNull String clientSecret) {
    return format(
        "%s/oauth/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
        baseServerUrl, encodeValue(clientId), encodeValue(clientSecret));
  }

  public String getPublicKey() {
    return format("%s/oauth/token/public_key", baseServerUrl);
  }

  public String createApplication() {
    return format("%s/applications", baseServerUrl);
  }

  public String updateApplication(@NonNull String applicationId) {
    return format("%s/applications/%s", baseServerUrl, applicationId);
  }

  public String listApplications(@NonNull ListApplicationRequest request) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam(OFFSET, request.getOffset())
        .optionalQuerySingleParam(LIMIT, request.getLimit())
        .optionalQuerySingleParam(QUERY, request.getQuery())
        .build(format("%s/applications", baseServerUrl));
  }

  public String createGroup() {
    return format("%s/groups", baseServerUrl);
  }

  public String listGroups(@NonNull ListGroupRequest request) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam(OFFSET, request.getOffset())
        .optionalQuerySingleParam(LIMIT, request.getLimit())
        .optionalQuerySingleParam(QUERY, request.getQuery())
        .build(format("%s/groups", baseServerUrl));
  }

  public String updateGroup(@NonNull String groupId) {
    return format("%s/groups/%s", baseServerUrl, groupId);
  }

  public String createPolicy() {
    return format("%s/policies", baseServerUrl);
  }

  public String updatePolicy(@NonNull String policyId) {
    return format("%s/policies/%s", baseServerUrl, policyId);
  }

  public String listPolicies(@NonNull ListPolicyRequest request) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam(OFFSET, request.getOffset())
        .optionalQuerySingleParam(LIMIT, request.getLimit())
        .optionalQuerySingleParam(QUERY, request.getQuery())
        .build(format("%s/policies", baseServerUrl));
  }

  public String createGroupPermission(@NonNull String groupId) {
    return format("%s/groups/%s/permissions", baseServerUrl, groupId);
  }

  public String listGroupPermissions(@NonNull ListGroupPermissionsRequest request) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam(OFFSET, request.getOffset())
        .optionalQuerySingleParam(LIMIT, request.getLimit())
        .optionalQuerySingleParam(QUERY, request.getQuery())
        .build(format("%s/policies/%s/groups", baseServerUrl, request.getPolicyId()));
  }

  @SneakyThrows
  private static String encodeValue(String value) {
    return encode(value, UTF_8.toString());
  }
}
