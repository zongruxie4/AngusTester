package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.FuncPlanAuthConverter.toFuncPlanAuths;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class FuncPlanAuthCmdImpl extends CommCmd<FuncPlanAuth, Long> implements FuncPlanAuthCmd {

  @Resource
  private FuncPlanAuthRepo funcPlanAuthRepo;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncPlanRepo funcPlanRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncPlanAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan funcPlanDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the func plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(auth.getPlanId());
        // Check the add creator permissions
        BizAssert.assertTrue(!funcPlanDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the user have func plan authorization permissions
        funcPlanAuthQuery.checkGrantAuth(getUserId(), auth.getPlanId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        // Check the for duplicate authorizations
        funcPlanAuthQuery.checkRepeatAuth(auth.getPlanId(), auth.getAuthObjectId(),
            auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add grant permission activity
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(CombinedTargetType.FUNC_PLAN, funcPlanDb,
              ActivityType.AUTH, authObjectName));
        }
        return insert(auth);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(FuncPlanAuth auth) {
    new BizTemplate<Void>() {
      FuncPlanAuth authDb;
      FuncPlan funcPlanDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the functional test plan authorization existed
        authDb = funcPlanAuthQuery.checkAndFind(auth.getId());
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the functional test plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(authDb.getPlanId());
        // Check the current user have functional test plan authorization permissions
        funcPlanAuthQuery.checkGrantAuth(getUserId(), authDb.getPlanId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Replace authorization
        authDb.setAuths(auth.getAuths());
        funcPlanAuthRepo.save(authDb);

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(CombinedTargetType.FUNC_PLAN, funcPlanDb,
              ActivityType.AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long planId, Boolean enabled) {
    new BizTemplate<Void>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Check the plan existed and authed
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
        // Check the user have plan authorization permissions
        funcPlanAuthQuery.checkGrantAuth(getUserId(), planId);
      }

      @Override
      protected Void process() {
        funcPlanRepo.updateAuthById(planId, enabled);
        funcCaseRepo.updatePlanAuthByPlanId(planId, enabled);

        // Enable permission control activity
        activityCmd.add(toActivity(CombinedTargetType.FUNC_PLAN, funcPlanDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      FuncPlanAuth authDb;
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Check the plan auth exists
        authDb = funcPlanAuthQuery.checkAndFind(id);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(authDb.getPlanId());
        // Check the user have plan authorization permissions
        funcPlanAuthQuery.checkGrantAuth(getUserId(), authDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Get if authorization object name
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // NOOP: Authorization can also be cancelled after the authorization object is deleted
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(CombinedTargetType.FUNC_PLAN, funcPlanDb,
            ActivityType.AUTH_CANCEL, authObjectName));

        // Delete plan permission
        funcPlanAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Override
  public void addCreatorAuth(Long planId, Set<Long> creatorIds) {
    batchInsert(toFuncPlanAuths(creatorIds, planId, FuncPlanPermission.ALL, true));
  }

  @Override
  public void addOwnerAndTesterAuth(Long planId, Long ownerId, Set<Long> testerIds) {
    // Add owner auths
    if (nonNull(ownerId)){
      batchInsert(toFuncPlanAuths(Set.of(ownerId), planId, FuncPlanPermission.ALL, false));
    }
    // Add tester auths
    testerIds.remove(getUserId());
    testerIds.remove(ownerId);
    if (isNotEmpty(testerIds)){
      batchInsert(toFuncPlanAuths(testerIds, planId, FuncPlanPermission.TESTER, false));
    }
  }

  @Override
  public void deleteAuthByPlanId(Long planId, Collection<Long> testerIds) {
    funcPlanAuthRepo.deleteByPlanIdAndAuthObjectId(planId, testerIds);
  }

  @Override
  protected BaseRepository<FuncPlanAuth, Long> getRepository() {
    return this.funcPlanAuthRepo;
  }
}
