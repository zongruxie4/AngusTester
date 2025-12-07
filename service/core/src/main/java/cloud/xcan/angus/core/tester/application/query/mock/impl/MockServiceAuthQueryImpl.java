package cloud.xcan.angus.core.tester.application.query.mock.impl;


import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_AUTH_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuthRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementation of MockServiceAuthQuery for managing Mock service authorization operations.
 * <p>
 * This class provides comprehensive functionality for querying and managing Mock service
 * authorization, including permission validation, user access control, and authorization management
 * for mock services and their associated resources.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock service authorization status checking and validation</li>
 *   <li>User permission retrieval and validation for various operations</li>
 *   <li>Batch permission checking for multiple services</li>
 *   <li>Authorization record management and conflict detection</li>
 *   <li>Creator rights validation and admin override support</li>
 *   <li>Public access control and permission bypass handling</li>
 *   <li>Organization-based authorization with user hierarchy support</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 *
 * @author XiaoLong Liu
 */
@Biz
public class MockServiceAuthQueryImpl implements MockServiceAuthQuery {

  @Resource
  private MockServiceAuthRepo mockServiceAuthRepo;
  @Resource
  private MockServiceQuery mockServiceQuery;
  @Resource
  private MockServiceInfoRepo mockServiceInfoRepo;
  @Resource
  private UserRepo userRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Retrieves the authorization control status for a specific Mock service.
   * <p>
   * Determines whether the Mock service has authorization control enabled, which affects how
   * permissions are enforced for service access and operations.
   *
   * @param serviceId the Mock service ID to check authorization status for
   * @return Boolean indicating whether authorization control is enabled
   */
  @Override
  public Boolean status(Long serviceId) {
    return new BizTemplate<Boolean>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Validate that the Mock service exists and retrieve it
        serviceDb = mockServiceQuery.checkAndFind(serviceId);
      }

      @Override
      protected Boolean process() {
        return serviceDb.getAuth();
      }
    }.execute();
  }

  /**
   * Retrieves user permissions for a specific Mock service with admin override support.
   * <p>
   * Determines the complete set of permissions available to a user for the specified Mock service,
   * considering admin privileges, creator rights, and service-specific authorization settings.
   * <p>
   * The method handles various permission scenarios including admin override, creator privileges,
   * public access, and explicit authorization grants.
   *
   * @param serviceId the Mock service ID to check permissions for
   * @param userId    the user ID to retrieve permissions for
   * @param admin     whether to consider admin privileges in permission calculation
   * @return List of MockServicePermission objects representing user permissions
   */
  @Override
  public List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean admin) {
    return new BizTemplate<List<MockServicePermission>>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Validate that the Mock service exists and retrieve it
        serviceDb = mockServiceQuery.checkAndFind(serviceId);
      }

      @Override
      protected List<MockServicePermission> process() {
        // Grant all permissions if user is admin and admin override is enabled
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return MockServicePermission.ALL;
        }

        // Retrieve user's authorization records for the service
        List<MockServiceAuth> auths = findAuth(userId, serviceId);

        // Grant all permissions if user is the creator of the service
        if (isCreator(auths)) {
          return MockServicePermission.ALL;
        }

        Set<MockServicePermission> permissions = new HashSet<>();

        // Grant view permission if authorization control is disabled
        if (!serviceDb.isEnabledAuth()) {
          permissions.add(MockServicePermission.VIEW);
        }

        // Collect all granted permissions from authorization records
        Set<MockServicePermission> authPermissions = auths.stream()
            .map(MockServiceAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  /**
   * Validates that a user has the specified permission for a Mock service.
   * <p>
   * Performs permission validation for a specific operation on a Mock service, throwing appropriate
   * exceptions if the user lacks the required permission.
   *
   * @param serviceId  the Mock service ID to check permissions for
   * @param permission the permission to validate
   * @param userId     the user ID to validate permissions for
   * @throws BizException if the user lacks the required permission
   */
  @Override
  public void check(Long serviceId, MockServicePermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, serviceId, permission);
        return null;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of authorization records for a Mock service.
   * <p>
   * Fetches authorization records with filtering and pagination support, ensuring the current user
   * has view permission for the service.
   *
   * @param serviceId the Mock service ID to retrieve authorizations for
   * @param spec      the search specification with criteria and filters
   * @param pageable  pagination parameters (page, size, sort)
   * @return Page of MockServiceAuth objects with authorization records
   */
  @Override
  public Page<MockServiceAuth> find(Long serviceId,
      GenericSpecification<MockServiceAuth> spec, Pageable pageable) {
    return new BizTemplate<Page<MockServiceAuth>>() {
      @Override
      protected void checkParams() {
        // Validate user authorization for viewing the service's authorization records
        checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Page<MockServiceAuth> process() {
        // Add service filter to search criteria for service-specific authorization retrieval
        spec.getCriteria().add(SearchCriteria.equal("mockServiceId", serviceId));
        return mockServiceAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Validates that an authorization record exists and retrieves it.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the authorization record is not
   * found in the system.
   *
   * @param id the authorization record ID to validate and retrieve
   * @return MockServiceAuth object if found
   * @throws ResourceNotFound if the authorization record is not found
   */
  @Override
  public MockServiceAuth checkAndFind(Long id) {
    return mockServiceAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockDatasourceAuth"));
  }

  /**
   * Validates that a user has ADD permission for a Mock service.
   * <p>
   * Convenience method for checking ADD permission, which allows users to create new resources
   * within the specified Mock service.
   *
   * @param userId    the user ID to validate permissions for
   * @param serviceId the Mock service ID to check permissions for
   * @throws BizException if the user lacks ADD permission
   */
  @Override
  public void checkAddAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.ADD);
  }

  /**
   * Validates that a user has VIEW permission for a Mock service.
   * <p>
   * Convenience method for checking VIEW permission, which allows users to access and view
   * resources within the specified Mock service.
   *
   * @param userId    the user ID to validate permissions for
   * @param serviceId the Mock service ID to check permissions for
   * @throws BizException if the user lacks VIEW permission
   */
  @Override
  public void checkViewAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.VIEW);
  }

  /**
   * Validates that a user has MODIFY permission for a Mock service.
   * <p>
   * Convenience method for checking MODIFY permission, which allows users to update and modify
   * resources within the specified Mock service.
   *
   * @param userId    the user ID to validate permissions for
   * @param serviceId the Mock service ID to check permissions for
   * @throws BizException if the user lacks MODIFY permission
   */
  @Override
  public void checkModifyAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.MODIFY);
  }

  /**
   * Validates that a user has DELETE permission for a Mock service.
   * <p>
   * Convenience method for checking DELETE permission, which allows users to remove resources
   * within the specified Mock service.
   *
   * @param userId    the user ID to validate permissions for
   * @param serviceId the Mock service ID to check permissions for
   * @throws BizException if the user lacks DELETE permission
   */
  @Override
  public void checkDeleteAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.DELETE);
  }

  /**
   * Validates that a user has GRANT permission for a Mock service.
   * <p>
   * Convenience method for checking GRANT permission, which allows users to manage authorization
   * for other users on the specified Mock service.
   * <p>
   * Note: Public APIs can be modified and authorized by anyone when public access is enabled,
   * bypassing normal authorization controls.
   *
   * @param userId    the user ID to validate permissions for
   * @param serviceId the Mock service ID to check permissions for
   * @throws BizException if the user lacks GRANT permission
   */
  @Override
  public void checkGrantAuth(Long userId, Long serviceId) {
    // Note: Public APIs can be modified and authorized by anyone when public access is enabled
    checkAuth(userId, serviceId, MockServicePermission.GRANT, false, true);
  }

  /**
   * Validates that a user has the specified permission for a Mock service.
   * <p>
   * Core permission validation method that checks user permissions against the specified Mock
   * service, considering admin privileges and public access.
   *
   * @param userId     the user ID to validate permissions for
   * @param apisId     the Mock service ID to check permissions for
   * @param permission the permission to validate
   * @throws BizException if the user lacks the required permission
   */
  @Override
  public void checkAuth(Long userId, Long apisId, MockServicePermission permission) {
    checkAuth(userId, apisId, permission, false, permission.isGrant());
  }

  /**
   * Validates that a user has the specified permission for a Mock service with advanced options.
   * <p>
   * Advanced permission validation method that provides fine-grained control over admin privilege
   * checking and public access handling.
   * <p>
   * The method handles various permission scenarios including admin override, public access bypass,
   * creator privileges, and explicit authorization grants.
   *
   * @param userId                the user ID to validate permissions for
   * @param datasourceId          the Mock service ID to check permissions for
   * @param permission            the permission to validate
   * @param ignoreAdminPermission whether to ignore admin privilege checking
   * @param ignorePublicAccess    whether to ignore public access bypass
   * @throws BizException if the user lacks the required permission
   */
  @Override
  public void checkAuth(Long userId, Long datasourceId, MockServicePermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    // Grant access if user is admin and admin permission checking is enabled
    if (!ignoreAdminPermission && commonQuery.isAdminUser()) {
      return;
    }

    // Note: When authorization control is disabled, non-grant permissions are bypassed
    // This prevents users without authorization permissions from being blocked
    if (!ignorePublicAccess && !permission.isGrant() && !mockServiceQuery
        .isAuthCtrl(datasourceId)) {
      return;
    }

    // Retrieve user's authorization records for the service
    List<MockServiceAuth> auths = findAuth(userId, datasourceId);

    // For VIEW permission, check if user has any authorization records
    if (permission.equals(MockServicePermission.VIEW)) {
      if (isEmpty(auths)) {
        throw BizException.of(MOCK_SERVICE_NO_AUTH_CODE, MOCK_SERVICE_NO_AUTH_T,
            new Object[]{permission});
      }
    }

    // Grant access if user is the creator of the service
    if (isCreator(auths)) {
      return;
    }

    // Check if user has the required permission in their authorization records
    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(MOCK_SERVICE_NO_AUTH_CODE, MOCK_SERVICE_NO_AUTH_T,
          new Object[]{permission});
    }
  }

  /**
   * Validates permissions for multiple Mock services in a single operation.
   * <p>
   * Performs batch permission validation across multiple services, providing efficient permission
   * checking for bulk operations.
   * <p>
   * The method handles admin privilege checking and provides detailed error information when
   * permission validation fails.
   *
   * @param serviceIds collection of Mock service IDs to validate permissions for
   * @param permission the permission to validate across all services
   * @throws BizException if the user lacks the required permission for any service
   */
  @Override
  public void batchCheckPermission(Collection<Long> serviceIds, MockServicePermission permission) {
    // Skip validation for admin users or empty service collections
    if (commonQuery.isAdminUser() || isEmpty(serviceIds) || isNull(permission)) {
      return;
    }

    // Filter services that require authorization control based on permission type
    Collection<Long> authIds = permission.isGrant()
        ? serviceIds : mockServiceInfoRepo.findIds0ByIdInAndAuth(serviceIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    // Retrieve user's authorization records for the filtered services
    List<MockServiceAuth> auths = findAuth(PrincipalContext.getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      MockServiceInfo serviceInfo = mockServiceInfoRepo.findById(firstId).orElse(null);
      throw BizException.of(MOCK_SERVICE_NO_TARGET_AUTH_CODE, MOCK_SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, isNull(serviceInfo) ? firstId : serviceInfo.getName()});
    }

    // Group authorization records by service ID for efficient permission checking
    Map<Long, List<MockServiceAuth>> authsMap = auths.stream()
        .filter(o -> nonNull(o.getMockServiceId()))
        .collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));

    // Validate permissions for each service
    for (Long mockServiceId : authsMap.keySet()) {
      List<MockServiceAuth> values = authsMap.get(mockServiceId);
      if (isNotEmpty(values)) {
        List<MockServicePermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).toList();
        if (isNotEmpty(permissions) && permissions.contains(permission)) {
          continue;
        }
      }
      MockServiceInfo serviceInfo = mockServiceInfoRepo.findById(mockServiceId).orElse(null);
      throw BizException.of(MOCK_SERVICE_NO_TARGET_AUTH_CODE, MOCK_SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, isNull(serviceInfo) ? mockServiceId : serviceInfo.getName()});
    }
  }

  /**
   * Validates that an authorization record does not already exist for the specified object.
   * <p>
   * Prevents duplicate authorization records by checking if an authorization already exists for the
   * specified auth object and service combination.
   *
   * @param serviceId      the Mock service ID to check for existing authorization
   * @param authObjectId   the auth object ID to validate
   * @param authObjectType the type of auth object (user, organization, etc.)
   * @throws ResourceExisted if an authorization record already exists
   */
  @Override
  public void checkRepeatAuth(Long serviceId, Long authObjectId, AuthObjectType authObjectType) {
    if (mockServiceAuthRepo.countByMockServiceIdAndAuthObjectIdAndAuthObjectType(serviceId,
        authObjectId, authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  /**
   * Finds Mock service IDs where a user has the specified permission.
   * <p>
   * Retrieves all Mock services where the specified user has the given permission, considering both
   * direct user permissions and organization-based permissions.
   *
   * @param userId     the user ID to find services for
   * @param permission the permission to search for
   * @return List of Mock service IDs where the user has the specified permission
   */
  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId,
      MockServicePermission permission) {
    // Retrieve user's organization IDs and include the user ID itself
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);

    // Find all authorization records for the user and their organizations
    return mockServiceAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(p -> p.getAuths().contains(permission)).map(MockServiceAuth::getMockServiceId)
        .toList();
  }

  /**
   * Retrieves authorization records for a user and a specific Mock service.
   * <p>
   * Fetches all authorization records for the specified user (including organization memberships)
   * and the specified Mock service.
   *
   * @param userId    the user ID to retrieve authorizations for
   * @param serviceId the Mock service ID to retrieve authorizations for
   * @return List of MockServiceAuth objects with authorization records
   */
  @Override
  public List<MockServiceAuth> findAuth(Long userId, Long serviceId) {
    // Retrieve user's organization IDs and include the user ID itself
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);

    // Find authorization records for the user and their organizations for the specific service
    return mockServiceAuthRepo.findAllByMockServiceIdAndAuthObjectIdIn(serviceId, orgIds);
  }

  /**
   * Retrieves authorization records for a user and multiple Mock services.
   * <p>
   * Fetches all authorization records for the specified user (including organization memberships)
   * and the specified Mock services, with efficient batch processing.
   *
   * @param userId     the user ID to retrieve authorizations for
   * @param serviceIds collection of Mock service IDs to retrieve authorizations for
   * @return List of MockServiceAuth objects with authorization records
   */
  @Override
  public List<MockServiceAuth> findAuth(Long userId, Collection<Long> serviceIds) {
    // Retrieve user's organization IDs and include the user ID itself
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);

    // Find authorization records based on whether specific services are provided
    return isEmpty(serviceIds) ? mockServiceAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : mockServiceAuthRepo.findAllByMockServiceIdInAndAuthObjectIdIn(serviceIds, orgIds);
  }

  /**
   * Determines if a user is the creator of a Mock service based on authorization records.
   * <p>
   * Checks if any of the user's authorization records for a service have the creator flag set,
   * indicating they created the service.
   *
   * @param auths list of authorization records to check for creator status
   * @return true if the user is the creator, false otherwise
   */
  private boolean isCreator(List<MockServiceAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (MockServiceAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Flattens authorization records into a set of permissions.
   * <p>
   * Extracts all permissions from the provided authorization records and returns them as a unified
   * set for efficient permission checking.
   *
   * @param auths list of authorization records to extract permissions from
   * @return Set of MockServicePermission objects representing all granted permissions
   */
  private Set<MockServicePermission> flatPermissions(List<MockServiceAuth> auths) {
    Set<MockServicePermission> permissions = new HashSet<>();
    for (MockServiceAuth auth : auths) {
      permissions.addAll(auth.getAuths());
    }
    return permissions;
  }
}
