package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
public class FuncPlanAuthQueryImpl implements FuncPlanAuthQuery {

  @Resource
  private FuncPlanAuthRepo funcPlanAuthRepo;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncPlanRepo funcPlanRepo;

  @Resource
  private UserRepo userRepo;

  @Override
  public Boolean status(Long planId) {
    return new BizTemplate<Boolean>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Check the func plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected Boolean process() {
        return funcPlanDb.getAuth();
      }
    }.execute();
  }

  @Override
  public List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean admin) {
    return new BizTemplate<List<FuncPlanPermission>>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Check the func plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected List<FuncPlanPermission> process() {
        if (Objects.nonNull(admin) && admin && isAdminUser()) {
          return FuncPlanPermission.ALL;
        }

        List<FuncPlanAuth> auths = findAuth(userId, planId);
        if (isCreator(auths)) {
          return FuncPlanPermission.ALL;
        }

        return auths.stream()
            .map(FuncPlanAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
      }
    }.execute();
  }

  @Override
  public FuncPlanAuthCurrent currentUserAuth(Long planId, Boolean admin) {
    return new BizTemplate<FuncPlanAuthCurrent>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Check the func plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected FuncPlanAuthCurrent process() {
        FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
        authCurrent.setFuncPlanAuth(funcPlanDb.getAuth());

        if (Objects.nonNull(admin) && admin && isAdminUser()) {
          authCurrent.addPermissions(FuncPlanPermission.ALL);
          return authCurrent;
        }

        List<FuncPlanAuth> auths = findAuth(getUserId(), planId);
        if (isCreator(auths)) {
          authCurrent.addPermissions(FuncPlanPermission.ALL);
          return authCurrent;
        }

        List<FuncPlanPermission> authPermissions = auths.stream()
            .map(FuncPlanAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public Map<Long, FuncPlanAuthCurrent> currentUserAuths(HashSet<Long> planIds, Boolean admin) {
    return new BizTemplate<Map<Long, FuncPlanAuthCurrent>>() {
      List<FuncPlan> planDb;

      @Override
      protected void checkParams() {
        // Check the func plan exists
        planDb = funcPlanQuery.checkAndFind(planIds);
      }

      @Override
      protected Map<Long, FuncPlanAuthCurrent> process() {
        Map<Long, FuncPlanAuthCurrent> authCurrentMap = new HashMap<>();
        if (nonNull(admin) && admin && isAdminUser()) {
          for (FuncPlan plan : planDb) {
            FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
            authCurrent.setFuncPlanAuth(plan.getAuth());
            authCurrent.addPermissions(FuncPlanPermission.ALL);
            authCurrentMap.put(plan.getId(), authCurrent);
          }
          return authCurrentMap;
        }

        Set<Long> currentCreatorIds = planDb.stream()
            .filter(x -> x.getCreatedBy().equals(getUserId())).map(FuncPlan::getId)
            .collect(Collectors.toSet());
        if (isNotEmpty(currentCreatorIds)) {
          for (FuncPlan plan : planDb) {
            if (currentCreatorIds.contains(plan.getId())) {
              FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
              authCurrent.setFuncPlanAuth(plan.getAuth());
              authCurrent.addPermissions(FuncPlanPermission.ALL);
              authCurrentMap.put(plan.getId(), authCurrent);
            }
          }
        }

        Set<Long> remainIds = new HashSet<>(planIds);
        remainIds.removeAll(currentCreatorIds);
        if (isNotEmpty(remainIds)) {
          Map<Long, List<FuncPlanAuth>> planAuthsMap = findAuth(getUserId(), remainIds)
              .stream().collect(Collectors.groupingBy(FuncPlanAuth::getPlanId));
          for (FuncPlan plan : planDb) {
            if (remainIds.contains(plan.getId())) {
              FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
              Set<FuncPlanPermission> permissions = new HashSet<>();
              List<FuncPlanAuth> scriptAuths = planAuthsMap.get(plan.getId());
              if (isNotEmpty(scriptAuths)) {
                Set<FuncPlanPermission> authPermissions = scriptAuths.stream()
                    .map(FuncPlanAuth::getAuths).flatMap(Collection::stream)
                    .collect(Collectors.toSet());
                permissions.addAll(authPermissions);
              }
              authCurrent.addPermissions(permissions);
              authCurrent.setFuncPlanAuth(plan.getAuth());
              authCurrentMap.put(plan.getId(), authCurrent);
            }
          }
        }
        return authCurrentMap;
      }
    }.execute();
  }

  @Override
  public void check(Long planId, FuncPlanPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, planId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<FuncPlanAuth> find(Specification<FuncPlanAuth> spec, List<String> planIds,
      Pageable pageable) {
    return new BizTemplate<Page<FuncPlanAuth>>() {
      @Override
      protected void checkParams() {
        if (isEmpty(planIds)) {
          throw ProtocolException.of(PARAM_MISSING_T, PARAM_MISSING_KEY, new Object[]{"planId"});
        }
      }

      @Override
      protected Page<FuncPlanAuth> process() {
        return funcPlanAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public FuncPlanAuth checkAndFind(Long id) {
    return funcPlanAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncPlanAuth"));
  }

  @Override
  public void checkModifyPlanAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.MODIFY_PLAN);
  }

  @Override
  public void checkDeletePlanAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.DELETE_PLAN);
  }

  @Override
  public void checkAddCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.ADD_CASE);
  }

  @Override
  public void checkModifyCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.MODIFY_CASE);
  }

  @Override
  public void checkExportCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.EXPORT_CASE);
  }

  @Override
  public void checkReviewAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.REVIEW);
  }

  @Override
  public void checkResetReviewResultAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.RESET_REVIEW_RESULT);
  }

  @Override
  public void checkTestAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.TEST);
  }

  @Override
  public void checkResetTestResultAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.RESET_TEST_RESULT);
  }

  @Override
  public void checkEstablishBaselineAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.ESTABLISH_BASELINE);
  }

  @Override
  public void checkGrantAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.GRANT, false, true);
  }

  @Override
  public void checkAuth(Long userId, Long planId, FuncPlanPermission permission) {
    checkAuth(userId, planId, permission, false, permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, Long planId, FuncPlanPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.notIgnorePublicAccess()
        && !funcPlanQuery.isAuthCtrl(planId)) {
      return;
    }

    List<FuncPlanAuth> planAuths = findAuth(userId, planId);

    if (isCreator(planAuths)) {
      return;
    }
    if (!findDirAction(planAuths).contains(permission)) {
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * Verify the operation permissions of the scenarioDir
   */
  @Override
  public void batchCheckPermission(Collection<Long> planIds, FuncPlanPermission permission) {
    if (isAdminUser() || isEmpty(planIds) || Objects.isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? planIds : funcPlanRepo.findIds0ByIdInAndAuth(planIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<FuncPlanAuth> auths = findAuth(PrincipalContext.getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      FuncPlan plan = funcPlanRepo.findById(firstId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(plan) ? firstId : plan.getName()});
    }

    Map<Long, List<FuncPlanAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getPlanId()))
        .collect(Collectors.groupingBy(FuncPlanAuth::getPlanId));
    for (Long planId : authMap.keySet()) {
      List<FuncPlanAuth> values = authMap.get(planId);
      if (isNotEmpty(values)) {
        List<FuncPlanPermission> planPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(planPermissions) && planPermissions.contains(permission)) {
          continue;
        }
      }
      FuncPlan plan = funcPlanRepo.findById(planId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(plan) ? planId : plan.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long planId, Long authObjectId, AuthObjectType authObjectType) {
    if (funcPlanAuthRepo.countByPlanIdAndAuthObjectIdAndAuthObjectType(planId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, FuncPlanPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return funcPlanAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(FuncPlanAuth::getPlanId).collect(
            Collectors.toList());
  }

  @Override
  public List<FuncPlanAuth> findAuth(Long userId, Long planId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return funcPlanAuthRepo.findAllByPlanIdAndAuthObjectIdIn(planId, orgIds);
  }

  @Override
  public List<FuncPlanAuth> findAuth(Long userId, Collection<Long> planIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(planIds) ? funcPlanAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : funcPlanAuthRepo.findAllByPlanIdInAndAuthObjectIdIn(planIds, orgIds);
  }

  @Override
  public List<FuncPlanPermission> getUserAuth(Long planId, Long userId) {
    if (isAdminUser()) {
      return FuncPlanPermission.ALL;
    }

    List<FuncPlanAuth> scenarioAuths = findAuth(userId, planId);
    if (isEmpty(scenarioAuths)) {
      return null;
    }
    if (isCreator(scenarioAuths)) {
      return FuncPlanPermission.ALL;
    }

    return scenarioAuths.stream().map(FuncPlanAuth::getAuths).flatMap(Collection::stream)
        .distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin();
  }

  @Override
  public boolean isCreator(Long userId, Long planId) {
    List<FuncPlanAuth> scenarioDirAuths = findAuth(userId, planId);
    return isCreator(scenarioDirAuths);
  }

  private boolean isCreator(List<FuncPlanAuth> planAuths) {
    if (planAuths.isEmpty()) {
      return false;
    }
    for (FuncPlanAuth auth : planAuths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  private Set<FuncPlanPermission> findDirAction(List<FuncPlanAuth> planAuths) {
    Set<FuncPlanPermission> actions = new HashSet<>();
    for (FuncPlanAuth auth : planAuths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}
