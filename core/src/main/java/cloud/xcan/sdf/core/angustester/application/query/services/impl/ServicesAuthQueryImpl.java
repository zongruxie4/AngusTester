package cloud.xcan.sdf.core.angustester.application.query.services.impl;

import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_NO_AUTH;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_NO_AUTH_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_NO_TARGET_AUTH;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_NO_TARGET_AUTH_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.commonlink.user.UserRepo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesRepo;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuth;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuthRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.exception.BizException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
        return serviceDb.getAuthFlag();
      }
    }.execute();
  }

  @Override
  public List<ServicesPermission> userAuth(Long serviceId, Long userId, Boolean adminFlag) {
    return new BizTemplate<List<ServicesPermission>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected List<ServicesPermission> process() {
        if (Objects.nonNull(adminFlag) && adminFlag && commonQuery.isAdminUser()) {
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

  @Override
  public ServicesAuthCurrent currentUserAuth(Long serviceId, Boolean adminFlag) {
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
        authCurrent.setServiceAuthFlag(serviceDb.getAuthFlag());

        if (Objects.nonNull(adminFlag) && adminFlag && commonQuery.isAdminUser()) {
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

  @Override
  public void check(Long serviceId, ServicesPermission permission, Long userId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        checkAuth(userId, serviceId, permission);
        return null;
      }
    }.execute();
  }

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

  @Override
  public ServicesAuth checkAndFind(Long id) {
    return servicesAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ServicesAuth"));
  }

  @Override
  public void checkAddAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.ADD);
  }

  @Override
  public void checkViewAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.DELETE);
  }

  @Override
  public void checkTestAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.TEST);
  }

  @Override
  public void checkGrantAuth(Long userId, Long serviceId) {
    // Fix:: Public service can be modified and authorized by anyone
    checkAuth(userId, serviceId, ServicesPermission.GRANT, false, true);
  }

  @Override
  public void checkShareAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, ServicesPermission.SHARE);
  }

  @Override
  public void checkReleaseAuth(Long userId, Long serviceId) {
    // Fix:: Release permission is also required for modifying the released status of public projects.
    checkAuth(userId, serviceId, ServicesPermission.RELEASE, false, false);
  }

  @Override
  public void checkAuth(Long userId, Long serviceId, ServicesPermission permission) {
    checkAuth(userId, serviceId, permission, false,
        permission.isGrant() || permission.isRelease());
  }

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
   * Verify the operation permissions of the service
   */
  @Override
  public void batchCheckPermission(Collection<Long> serviceIds, ServicesPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(serviceIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? serviceIds : servicesRepo.findIds0ByIdInAndAuthFlag(serviceIds, true);
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
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(projectPermissions) && projectPermissions.contains(permission)) {
          continue;
        }
      }
      Services service = servicesRepo.findById(serviceId).orElse(null);
      throw BizException.of(SERVICE_NO_TARGET_AUTH_CODE, SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(service) ? serviceId : service.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long serviceId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creatorFlag) {
    if (servicesAuthRepo.countByServiceIdAndAuthObjectIdAndAuthObjectTypeAndCreatorFlag(serviceId,
        authObjectId, authObjectType, creatorFlag) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ServicesPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return servicesAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(p -> p.getAuths().contains(permission)).map(ServicesAuth::getServiceId)
        .distinct().collect(Collectors.toList());
  }

  @Override
  public List<ServicesAuth> findAuth(Long userId, Long serviceId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return servicesAuthRepo.findByServiceIdAndAuthObjectIdIn(serviceId, orgIds);
  }

  @Override
  public List<ServicesAuth> findAuth(Long userId, Collection<Long> serviceIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(serviceIds) ? servicesAuthRepo.findAllByAuthObjectIdIn(orgIds) :
        servicesAuthRepo.findByServiceIdInAndAuthObjectIdIn(serviceIds, orgIds);
  }

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
        .distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isCreator(Long userId, Long serviceId) {
    List<ServicesAuth> projectAuths = findAuth(userId, serviceId);
    return isCreator(projectAuths);
  }

  private boolean isCreator(List<ServicesAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ServicesAuth auth : auths) {
      if (auth.getCreatorFlag()) {
        return true;
      }
    }
    return false;
  }

  private Set<ServicesPermission> flatPermissions(List<ServicesAuth> auths) {
    Set<ServicesPermission> actions = new HashSet<>();
    for (ServicesAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}




