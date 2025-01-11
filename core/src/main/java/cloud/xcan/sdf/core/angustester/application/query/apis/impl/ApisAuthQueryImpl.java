package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_NO_AUTH;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_NO_AUTH_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_NO_TARGET_AUTH;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_NO_TARGET_AUTH_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.api.commonlink.user.UserRepo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuth;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuthRepo;
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
public class ApisAuthQueryImpl implements ApisAuthQuery {

  @Resource
  private ApisAuthRepo apisAuthRepo;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private UserRepo userRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Boolean status(Long apiId) {
    return new BizTemplate<Boolean>() {
      ApisBaseInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Check and get apis exists
        apiInfoDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected Boolean process() {
        return apiInfoDb.getAuthFlag();
      }
    }.execute();
  }

  @Override
  public List<ApiPermission> userAuth(Long apiId, Long userId, Boolean adminFlag) {
    return new BizTemplate<List<ApiPermission>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected List<ApiPermission> process() {
        if (Objects.nonNull(adminFlag) && adminFlag && commonQuery.isAdminUser()) {
          return ApiPermission.ALL;
        }

        List<ApisAuth> apisAuths = findAuth(userId, apiId);
        if (isCreator(apisAuths)) {
          return ApiPermission.ALL;
        }

        Set<ApiPermission> permissions = new HashSet<>();
        if (!apisDb.isEnabledAuth()) {
          permissions.add(ApiPermission.VIEW);
          permissions.add(ApiPermission.DEBUG);
        }

        Set<ApiPermission> authPermissions = apisAuths.stream().map(ApisAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  @Override
  public ApisAuthCurrent currentUserAuth(Long apiId, Boolean adminFlag) {
    return new BizTemplate<ApisAuthCurrent>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected ApisAuthCurrent process() {
        ApisAuthCurrent authCurrent = new ApisAuthCurrent();
        authCurrent.setProjectAuthFlag(apisDb.getServiceAuthFlag());
        authCurrent.setApisAuthFlag(apisDb.getAuthFlag());

        if (Objects.nonNull(adminFlag) && adminFlag && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(ApiPermission.ALL);
          return authCurrent;
        }

        List<ApisAuth> apisAuths = findAuth(getUserId(), apiId);
        if (isCreator(apisAuths)) {
          authCurrent.addPermissions(ApiPermission.ALL);
          return authCurrent;
        }

        Set<ApiPermission> permissions = new HashSet<>();
        if (!apisDb.isEnabledAuth()) {
          permissions.add(ApiPermission.VIEW);
          permissions.add(ApiPermission.DEBUG);
        }

        Set<ApiPermission> authPermissions = apisAuths.stream().map(ApisAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public void check(Long apisId, ApiPermission permission, Long userId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        checkAuth(userId, apisId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<ApisAuth> find(List<String> apiIds, Specification<ApisAuth> spec,
      Pageable pageable) {
    return new BizTemplate<Page<ApisAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(apiIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"apisId"});
        batchCheckPermission(apiIds.stream().map(Long::parseLong)
            .collect(Collectors.toSet()), ApiPermission.VIEW);
      }

      @Override
      protected Page<ApisAuth> process() {
        return apisAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ApisAuth checkAndFind(Long id) {
    return apisAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApiAuth"));
  }

  @Override
  public void checkViewAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.DELETE);
  }

  @Override
  public void checkDebugAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.DEBUG);
  }

  @Override
  public void checkTestAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.TEST);
  }

  @Override
  public void checkGrantAuth(Long userId, Long apisId) {
    // Fix:: Public apis can be modified and authorized by anyone
    checkAuth(userId, apisId, ApiPermission.GRANT, false, true);
  }

  @Override
  public void checkShareAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.SHARE);
  }

  @Override
  public void checkReleaseAuth(Long userId, Long apisId) {
    // Fix:: Release permission is also required for modifying the released status of public apis.
    checkAuth(userId, apisId, ApiPermission.RELEASE, false, true);
  }

  @Override
  public void checkExportAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.EXPORT);
  }

  @Override
  public void checkAuth(Long userId, Long apisId, ApiPermission permission) {
    checkAuth(userId, apisId, permission, false,
        permission.isRelease() || permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, Long apisId, ApiPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !permission.isRelease()
        && !apisQuery.isAuthCtrl(apisId)) {
      return;
    }

    // View as base permissions
    List<ApisAuth> auths = findAuth(userId, apisId);
    if (permission.equals(ApiPermission.VIEW)) {
      if (isEmpty(auths)) {
        throw BizException.of(APIS_NO_AUTH_CODE, APIS_NO_AUTH, new Object[]{permission});
      }
    }

    if (isCreator(auths)) {
      return;
    }

    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(APIS_NO_AUTH_CODE, APIS_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * Verify the operation permissions of the apis
   */
  @Override
  public void batchCheckPermission(Collection<Long> apiIds, ApiPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(apiIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? apiIds : apisBaseInfoRepo.findIds0ByIdInAndAuthFlag(apiIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ApisAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      ApisBaseInfo apisBaseInfo = apisBaseInfoRepo.findById(firstId).orElse(null);
      throw BizException.of(APIS_NO_TARGET_AUTH_CODE, APIS_NO_TARGET_AUTH, new Object[]{permission,
          isNull(apisBaseInfo) ? firstId : apisBaseInfo.getName()});
    }

    Map<Long, List<ApisAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getApisId())).collect(Collectors.groupingBy(ApisAuth::getApisId));
    for (Long apiId : authMap.keySet()) {
      List<ApisAuth> values = authMap.get(apiId);
      if (isNotEmpty(values)) {
        List<ApiPermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(permissions) && permissions.contains(permission)) {
          continue;
        }
      }
      ApisBaseInfo apisBaseInfo = apisBaseInfoRepo.findById(apiId).orElse(null);
      throw BizException.of(APIS_NO_TARGET_AUTH_CODE, APIS_NO_TARGET_AUTH, new Object[]{permission,
          isNull(apisBaseInfo) ? apiId : apisBaseInfo.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creatorFlag) {
    if (apisAuthRepo.countByApisIdAndAuthObjectIdAndAuthObjectTypeAndCreatorFlag(apisId,
        authObjectId, authObjectType, creatorFlag) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType) {
    if (apisAuthRepo
        .countByApisIdAndAuthObjectIdAndAuthObjectType(apisId, authObjectId, authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<ApisAuth> findAuth(Long userId, Long apisId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return apisAuthRepo.findAllByApisIdAndAuthObjectIdIn(apisId, orgIds);
  }

  @Override
  public List<ApisAuth> findAuth(Long userId, Collection<Long> apisIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(apisIds) ? apisAuthRepo.findAllByAuthObjectIdIn(orgIds) :
        apisAuthRepo.findAllByApisIdInAndAuthObjectIdIn(apisIds, orgIds);
  }

  @Override
  public List<ApiPermission> getUserAuth(Long apiId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return ApiPermission.ALL;
    }

    List<ApisAuth> apisAuths = findAuth(userId, apiId);
    if (isEmpty(apisAuths)) {
      return null;
    }
    if (isCreator(apisAuths)) {
      return ApiPermission.ALL;
    }
    return apisAuths.stream().map(ApisAuth::getAuths).flatMap(Collection::stream)
        .distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isCreator(Long userId, Long apisId) {
    List<ApisAuth> apisAuths = findAuth(userId, apisId);
    return isCreator(apisAuths);
  }

  private boolean isCreator(List<ApisAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ApisAuth auth : auths) {
      if (auth.getCreatorFlag()) {
        return true;
      }
    }
    return false;
  }

  private Set<ApiPermission> flatPermissions(List<ApisAuth> auths) {
    Set<ApiPermission> actions = new HashSet<>();
    for (ApisAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}
