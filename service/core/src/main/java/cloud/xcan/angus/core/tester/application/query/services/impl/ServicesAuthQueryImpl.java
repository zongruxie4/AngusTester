package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthCurrent;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
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
import org.springframework.data.jpa.domain.Specification;

/**
 * <p>
 * Implementation of ServicesAuthQuery for services authorization management and validation.
 * </p>
 * <p>
 * Provides methods for checking services permissions, managing user authorizations, and validating access rights.
 * </p>
 */
@Biz
public class ServicesAuthQueryImpl implements ServicesAuthQuery {

  @Resource
  private ServicesAuthRepo servicesAuthRepo;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesRepo servicesRepo;
  @Resource
  private UserRepo userRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * <p>
   * Get the authorization status of a service.
   * </p>
   * @param serviceId Service ID
   * @return Authorization status
   */
  @Override
  public Boolean status(Long serviceId) {
    return new BizTemplate<Boolean>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Boolean process() {
        return serviceDb.getAuth();
      }
    }.execute();
  }

  /**
   * <p>
   * Get user permissions for a specific service.
   * </p>
   * <p>
   * Returns all permissions for admins and creators. For regular users, returns permissions based on their authorization records.
   * </p>
   * @param serviceId Service ID
   * @param userId User ID
   * @param admin Whether to check admin permissions
   * @return List of services permissions
   */
  @Override
  public List<ServicesPermission> userAuth(Long serviceId, Long userId, Boolean admin) {
    return new BizTemplate<List<ServicesPermission>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected List<ServicesPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return ServicesPermission.ALL;
        }

        List<ServicesAuth> auths = findAuth(userId, serviceId);
        if (isCreator(auths)) {
          return ServicesPermission.ALL;
        }

        Set<ServicesPermission> permissions = new HashSet<>();
        if (!serviceDb.isEnabledAuth()) {
          permissions.add(ServicesPermission.VIEW);
          //permissions.add(ServicesPermission.DEBUG);
        }

        Set<ServicesPermission> authPermissions = auths.stream().map(ServicesAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  /**
   * <p>
   * Get current user's authorization information for a service.
   * </p>
   * <p>
   * Returns authorization status and permissions for the current user.
   * </p>
   * @param serviceId Service ID
   * @param admin Whether to check admin permissions
   * @return Current user's authorization information
   */
  @Override
  public ServicesAuthCurrent currentUserAuth(Long serviceId, Boolean admin) {
    return new BizTemplate<ServicesAuthCurrent>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected ServicesAuthCurrent process() {
        ServicesAuthCurrent authCurrent = new ServicesAuthCurrent();
        authCurrent.setServiceAuth(serviceDb.getAuth());

        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(ServicesPermission.ALL);
          return authCurrent;
        }

        List<ServicesAuth> auths = findAuth(getUserId(), serviceId);
        if (isCreator(auths)) {
          authCurrent.addPermissions(ServicesPermission.ALL);
          return authCurrent;
        }

        Set<ServicesPermission> permissions = new HashSet<>();
        if (!serviceDb.isEnabledAuth()) {
          permissions.add(ServicesPermission.VIEW);
          //permissions.add(ServicesPermission.DEBUG);
        }

        Set<ServicesPermission> authPermissions = auths.stream().map(ServicesAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if a user has a specific permission for a service.
   * </p>
   * @param serviceId Service ID
   * @param permission Required permission
   * @param userId User ID
   */
  @Override
  public void check(Long serviceId, ServicesPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, serviceId, permission);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Find service authorizations with pagination and permission validation.
   * </p>
   * @param spec Search specification
   * @param serviceIds List of service IDs
   * @param pageable Pagination information
   * @return Page of service authorizations
   */
  @Override
  public Page<ServicesAuth> find(Specification<ServicesAuth> spec, List<String> serviceIds,
      Pageable pageable) {
    return new BizTemplate<Page<ServicesAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(serviceIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"serviceId"});
        batchCheckPermission(serviceIds.stream().map(Long::parseLong)
            .collect(Collectors.toSet()), ServicesPermission.VIEW);
      }

      @Override
      protected Page<ServicesAuth> process() {
        return servicesAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find a service authorization by ID.
   * </p>
   * @param id Authorization ID
   * @return Service authorization entity
   */
  @Override
  public ServicesAuth checkAndFind(Long id) {
    return servicesAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ServicesAuth"));
  }

  /**
   * <p>
   * Check if a user has add permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkAddAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.ADD);
  }

  /**
   * <p>
   * Check if a user has view permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkViewAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.VIEW);
  }

  /**
   * <p>
   * Check if a user has modify permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkModifyAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.MODIFY);
  }

  /**
   * <p>
   * Check if a user has delete permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkDeleteAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.DELETE);
  }

  /**
   * <p>
   * Check if a user has test permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkTestAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.TEST);
  }

  /**
   * <p>
   * Check if a user has grant permission for a service.
   * </p>
   * <p>
   * Fix: Public service can be modified and authorized by anyone.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkGrantAuth(Long userId, Long serviceId) {
    // Fix:: Public service can be modified and authorized by anyone
    checkAuth(userId, serviceId, ServicesPermission.GRANT, false, true);
  }

  /**
   * <p>
   * Check if a user has share permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkShareAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.SHARE);
  }

  /**
   * <p>
   * Check if a user has release permission for a service.
   * </p>
   * <p>
   * Fix: Release permission is also required for modifying the released status of public projects.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   */
  @Override
  public void checkReleaseAuth(Long userId, Long serviceId) {
    // Fix:: Release permission is also required for modifying the released status of public projects.
    checkAuth(userId, serviceId, ServicesPermission.RELEASE, false, false);
  }

  /**
   * <p>
   * Check if a user has a specific permission for a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   * @param permission Required permission
   */
  @Override
  public void checkAuth(Long userId, Long serviceId, ServicesPermission permission) {
    checkAuth(userId, serviceId, permission, false,
        permission.isGrant() || permission.isRelease());
  }

  /**
   * <p>
   * Check if a user has a specific permission for a service with additional control options.
   * </p>
   * <p>
   * Admins bypass permission checks unless explicitly ignored. Public access is allowed for non-grant and non-release permissions
   * when authorization is not controlled.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   * @param permission Required permission
   * @param ignoreAdminPermission Whether to ignore admin permissions
   * @param ignorePublicAccess Whether to ignore public access
   */
  @Override
  public void checkAuth(Long userId, Long serviceId, ServicesPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !permission.isRelease()
        && !servicesQuery.isAuthCtrl(serviceId)) {
      return;
    }

    List<ServicesAuth> apisAuths = findAuth(userId, serviceId);
    if (permission.equals(ServicesPermission.VIEW)) {
      // View as base permissions
      if (isEmpty(apisAuths)) {
        throw BizException.of(SERVICE_NO_AUTH_CODE, SERVICE_NO_AUTH, new Object[]{permission});
      }
    }

    if (isCreator(apisAuths)) {
      return;
    }

    if (!flatPermissions(apisAuths).contains(permission)) {
      throw BizException.of(SERVICE_NO_AUTH_CODE, SERVICE_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * <p>
   * Verify the operation permissions of the service for multiple services.
   * </p>
   * <p>
   * Performs batch permission checking for a collection of services. Admins bypass this check.
   * Only services with authorization enabled are checked for non-grant permissions.
   * </p>
   * @param serviceIds Collection of service IDs
   * @param permission Required permission
   */
  @Override
  public void batchCheckPermission(Collection<Long> serviceIds, ServicesPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(serviceIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? serviceIds : servicesRepo.findIds0ByIdInAndAuth(serviceIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ServicesAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      Services service = servicesRepo.findById(firstId).orElse(null);
      throw BizException.of(SERVICE_NO_TARGET_AUTH_CODE, SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, isNull(service) ? firstId : service.getName()});
    }

    Map<Long, List<ServicesAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getServiceId()))
        .collect(Collectors.groupingBy(ServicesAuth::getServiceId));
    for (Long serviceId : authMap.keySet()) {
      List<ServicesAuth> values = authMap.get(serviceId);
      if (isNotEmpty(values)) {
        List<ServicesPermission> projectPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).toList();
        if (isNotEmpty(projectPermissions) && projectPermissions.contains(permission)) {
          continue;
        }
      }
      Services service = servicesRepo.findById(serviceId).orElse(null);
      throw BizException.of(SERVICE_NO_TARGET_AUTH_CODE, SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(service) ? serviceId : service.getName()});
    }
  }

  /**
   * <p>
   * Check if an authorization already exists for the specified service and auth object.
   * </p>
   * @param serviceId Service ID
   * @param authObjectId Authorization object ID
   * @param authObjectType Authorization object type
   * @param creator Whether the auth object is a creator
   */
  @Override
  public void checkRepeatAuth(Long serviceId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creator) {
    if (servicesAuthRepo.countByServiceIdAndAuthObjectIdAndAuthObjectTypeAndCreator(serviceId,
        authObjectId, authObjectType, creator) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  /**
   * <p>
   * Find service IDs where a user has a specific permission through their organization memberships.
   * </p>
   * @param userId User ID
   * @param permission Required permission
   * @return List of service IDs
   */
  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ServicesPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return servicesAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(p -> p.getAuths().contains(permission)).map(ServicesAuth::getServiceId)
        .distinct().toList();
  }

  /**
   * <p>
   * Find authorization records for a user and a specific service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   * @return List of service authorizations
   */
  @Override
  public List<ServicesAuth> findAuth(Long userId, Long serviceId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return servicesAuthRepo.findByServiceIdAndAuthObjectIdIn(serviceId, orgIds);
  }

  /**
   * <p>
   * Find authorization records for a user and multiple services.
   * </p>
   * @param userId User ID
   * @param serviceIds Collection of service IDs
   * @return List of service authorizations
   */
  @Override
  public List<ServicesAuth> findAuth(Long userId, Collection<Long> serviceIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(serviceIds) ? servicesAuthRepo.findAllByAuthObjectIdIn(orgIds) :
        servicesAuthRepo.findByServiceIdInAndAuthObjectIdIn(serviceIds, orgIds);
  }

  /**
   * <p>
   * Get user permissions for a specific service.
   * </p>
   * <p>
   * Returns all permissions for admins and creators. For regular users, returns their specific permissions.
   * </p>
   * @param serviceId Service ID
   * @param userId User ID
   * @return List of user permissions, or null if no permissions found
   */
  @Override
  public List<ServicesPermission> getUserAuth(Long serviceId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return ServicesPermission.ALL;
    }

    List<ServicesAuth> projectAuths = findAuth(userId, serviceId);
    if (isEmpty(projectAuths)) {
      return null;
    }
    if (isCreator(projectAuths)) {
      return ServicesPermission.ALL;
    }

    return projectAuths.stream().map(ServicesAuth::getAuths).flatMap(Collection::stream)
        .distinct().toList();
  }

  /**
   * <p>
   * Check if a user is the creator of a service.
   * </p>
   * @param userId User ID
   * @param serviceId Service ID
   * @return true if the user is the creator, false otherwise
   */
  @Override
  public boolean isCreator(Long userId, Long serviceId) {
    List<ServicesAuth> projectAuths = findAuth(userId, serviceId);
    return isCreator(projectAuths);
  }

  /**
   * <p>
   * Check if any of the authorization records indicates the user is a creator.
   * </p>
   * @param auths List of service authorizations
   * @return true if the user is a creator, false otherwise
   */
  private boolean isCreator(List<ServicesAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ServicesAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>
   * Flatten all permissions from a list of authorization records into a single set.
   * </p>
   * @param auths List of service authorizations
   * @return Set of all permissions
   */
  private Set<ServicesPermission> flatPermissions(List<ServicesAuth> auths) {
    Set<ServicesPermission> actions = new HashSet<>();
    for (ServicesAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}




