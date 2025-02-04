package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncPlanAuthConverter.toFuncPlanAuths;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncPlanAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Resource;
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
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
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
  public void enabled(Long planId, Boolean enabledFlag) {
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
        funcPlanRepo.updateAuthFlagById(planId, enabledFlag);
        funcCaseRepo.updatePlanAuthFlagByPlanId(planId, enabledFlag);

        // Enable permission control activity
        activityCmd.add(toActivity(CombinedTargetType.FUNC_PLAN, funcPlanDb,
            enabledFlag ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
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
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
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
