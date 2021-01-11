package bio.overture.dms.ego.client;

import static bio.overture.dms.ego.exception.EgoClientException.checkEgoClient;
import static com.google.common.base.Preconditions.checkState;

import bio.overture.dms.ego.exception.EgoClientException;
import bio.overture.dms.ego.model.ApplicationRequest;
import bio.overture.dms.ego.model.EgoApplication;
import bio.overture.dms.ego.model.EgoGroup;
import bio.overture.dms.ego.model.EgoPolicy;
import bio.overture.dms.ego.model.GroupPermission;
import bio.overture.dms.ego.model.GroupRequest;
import bio.overture.dms.ego.model.ListApplicationRequest;
import bio.overture.dms.ego.model.ListGroupPermissionsRequest;
import bio.overture.dms.ego.model.ListGroupRequest;
import bio.overture.dms.ego.model.ListPolicyRequest;
import bio.overture.dms.ego.model.PermissionMasks;
import bio.overture.dms.ego.model.PermissionRequest;
import bio.overture.dms.ego.model.PolicyRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Convenience service that simplifies popular interaction with a running ego server, using the
 * EgoClient and (mostly) idempotent methods.
 */
@RequiredArgsConstructor
public class EgoService {

  @NonNull private final EgoClient client;

  /**
   * For an existing group and policy, creates a GroupPermission only if a permission was not
   * already created. * If a permission already exists, nothing happens.
   *
   * @param groupName name of an existing group
   * @param policyName name of an existing policy
   * @param permissionMask accessLevel or mask of the permission
   * @throws EgoClientException if group or policy does not already exist
   */
  public void createGroupPermission(
      @NonNull String groupName,
      @NonNull String policyName,
      @NonNull PermissionMasks permissionMask) {
    val groupResult = findGroupByName(groupName);
    val policyResult = findPolicyByName(policyName);

    // Check groups and policy exist
    checkEgoClient(
        groupResult.isPresent(),
        "Cannot create group permission since the group '%s' does not exist",
        groupName);
    checkEgoClient(
        policyResult.isPresent(),
        "Cannot create group permission since the policy '%s' does not exist",
        policyName);

    val group = groupResult.get();
    val policy = policyResult.get();
    val groupId = group.getId();

    // If the groupPermission already exists do nothing, otherwise, create the permission
    findGroupPermissionByPolicyName(groupId, policyName)
        .ifPresentOrElse(
            x -> {},
            () ->
                client.createGroupPermission(
                    groupId,
                    List.of(
                        PermissionRequest.builder()
                            .policyId(policy.getId())
                            .mask(permissionMask)
                            .build())));
  }

  /**
   * Creates a group if it does not already exist. Otherwise, will update a group matching the
   * {@code groupName} with the {@code groupRequest}. Note if the {@code groupRequest} has a
   * different name field, then the group will be renamed to that new name.
   *
   * @param groupName name of group to create or update
   * @param groupRequest body of the group to create or update. In the case of creation, the
   *     {\@param groupName} must equal the {@param groupRequest.name} field
   * @return group
   */
  public EgoGroup saveGroup(@NonNull String groupName, @NonNull GroupRequest groupRequest) {
    val result = findGroupByName(groupName);
    if (result.isPresent()) {
      val group = result.get();
      if (isUpdatedNeeded(group, groupRequest)) {
        val groupId = group.getId();
        return client.updateGroup(groupId, groupRequest);
      }
      return group;
    } else {
      checkState(
          groupName.equals(groupRequest.getName()),
          "Cannot create Group with name '%s' since request does not contain the same name",
          groupName);
      return client.createGroup(groupRequest);
    }
  }

  public EgoGroup saveGroup(@NonNull GroupRequest r) {
    return saveGroup(r.getName(), r);
  }

  public EgoPolicy savePolicy(@NonNull PolicyRequest r) {
    return savePolicy(r.getName(), r);
  }

  public EgoPolicy savePolicy(@NonNull String policyName, @NonNull PolicyRequest r) {
    val result = findPolicyByName(policyName);
    if (result.isPresent()) {
      val policy = result.get();
      if (isUpdatedNeeded(policy, r)) {
        val policyId = policy.getId();
        return client.updatePolicy(policyId, r);
      }
      return policy;
    } else {
      checkState(
          policyName.equals(r.getName()),
          "Cannot create Policy with name '%s' since request does not contain the same name",
          policyName);
      return client.createPolicy(r);
    }
  }

  /** Create or Update an existing application. This should be idempotent */
  public EgoApplication saveApplication(
      @NonNull String applicationName, @NonNull ApplicationRequest r) {
    val result = findApplicationByName(r.getName());
    if (result.isPresent()) {
      val app = result.get();
      if (isUpdatedNeeded(app, r)) {
        val appId = app.getId();
        return client.updateApplication(appId, r);
      }
      return app;
    } else {
      checkState(
          applicationName.equals(r.getName()),
          "Cannot create Application with name '%s' since request does not contain the same name",
          applicationName);
      return client.createApplication(r);
    }
  }

  public EgoApplication saveApplication(@NonNull ApplicationRequest r) {
    return saveApplication(r.getName(), r);
  }

  public Optional<GroupPermission> findGroupPermissionByPolicyName(
      @NonNull String groupId, @NonNull String policyName) {
    val listRequest = ListGroupPermissionsRequest.builder().id(groupId).offset(0).limit(30).build();
    while (true) {
      val page = client.listGroupPermissions(listRequest);
      if (page.getResultSet().isEmpty()) {
        return Optional.empty();
      }
      val result =
          page.getResultSet().stream()
              .filter(x -> x.getPolicy().getName().equals(policyName))
              .findFirst();
      if (result.isPresent()) {
        return result;
      } else {
        listRequest.setOffset(listRequest.getOffset() + listRequest.getLimit());
      }
    }
  }

  public Optional<EgoApplication> findApplicationByName(@NonNull String applicationName) {
    val listAppRequest = new ListApplicationRequest();
    listAppRequest.setOffset(0);
    listAppRequest.setLimit(30);
    listAppRequest.setQuery(applicationName);

    while (true) {
      val page = client.listApplications(listAppRequest);
      if (page.getResultSet().isEmpty()) {
        return Optional.empty();
      }
      val result =
          page.getResultSet().stream().filter(x -> x.getName().equals(applicationName)).findFirst();
      if (result.isPresent()) {
        return result;
      } else {
        listAppRequest.setOffset(listAppRequest.getOffset() + listAppRequest.getLimit());
      }
    }
  }

  public Optional<EgoGroup> findGroupByName(@NonNull String groupName) {
    val listGroupRequest = new ListGroupRequest();
    listGroupRequest.setOffset(0);
    listGroupRequest.setLimit(30);
    listGroupRequest.setQuery(groupName);

    while (true) {
      val page = client.listGroups(listGroupRequest);
      if (page.getResultSet().isEmpty()) {
        return Optional.empty();
      }
      val result =
          page.getResultSet().stream().filter(x -> x.getName().equals(groupName)).findFirst();
      if (result.isPresent()) {
        return result;
      } else {
        listGroupRequest.setOffset(listGroupRequest.getOffset() + listGroupRequest.getLimit());
      }
    }
  }

  public Optional<EgoPolicy> findPolicyByName(@NonNull String policyName) {
    val listPolicyRequest = new ListPolicyRequest();
    listPolicyRequest.setOffset(0);
    listPolicyRequest.setLimit(30);
    listPolicyRequest.setQuery(policyName);

    while (true) {
      val page = client.listPolicies(listPolicyRequest);
      if (page.getResultSet().isEmpty()) {
        return Optional.empty();
      }
      val result =
          page.getResultSet().stream().filter(x -> x.getName().equals(policyName)).findFirst();
      if (result.isPresent()) {
        return result;
      } else {
        listPolicyRequest.setOffset(listPolicyRequest.getOffset() + listPolicyRequest.getLimit());
      }
    }
  }

  public static EgoService createEgoService(EgoClient egoClient) {
    return new EgoService(egoClient);
  }

  private static boolean isUpdatedNeeded(@NonNull EgoGroup egoGroup, @NonNull GroupRequest r) {
    return !(Objects.equals(egoGroup.getDescription(), r.getDescription())
        && Objects.equals(egoGroup.getName(), r.getName())
        && Objects.equals(egoGroup.getStatus(), r.getStatus()));
  }

  private static boolean isUpdatedNeeded(@NonNull EgoPolicy egoPolicy, @NonNull PolicyRequest r) {
    return !(Objects.equals(egoPolicy.getName(), r.getName()));
  }

  private static boolean isUpdatedNeeded(
      @NonNull EgoApplication egoApplication, @NonNull ApplicationRequest r) {
    return !(Objects.equals(egoApplication.getDescription(), r.getDescription())
        && Objects.equals(egoApplication.getName(), r.getName())
        && Objects.equals(egoApplication.getStatus(), r.getStatus())
        && Objects.equals(egoApplication.getClientId(), r.getClientId())
        && Objects.equals(egoApplication.getClientSecret(), r.getClientSecret())
        && Objects.equals(egoApplication.getRedirectUri(), r.getRedirectUri())
        && Objects.equals(egoApplication.getType(), r.getType()));
  }
}
