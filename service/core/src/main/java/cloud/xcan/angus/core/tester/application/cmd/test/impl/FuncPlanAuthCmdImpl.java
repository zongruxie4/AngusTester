package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.FuncPlanAuthConverter.toFuncPlanAuths;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncPlanAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional plan authorization.
 * </p>
 * <p>
 * Provides methods for adding, updating, replacing, enabling, and deleting plan authorizations.
 * Handles permission checks, authorization validation, and activity logging.
 * </p>
 * <p>
 * Key features include authorization management, creator permission setup, owner and tester
 * authorization, and comprehensive activity tracking.
 * </p>
 */
@Service
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

  /**
   * Add a new authorization for a functional test plan.
   * <p>
   * Checks plan existence, permission, and duplicate authorization before adding.
   * <p>
   * Logs grant permission activity.
   */
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

  /**
   * Replace (update) an existing authorization for a functional test plan.
   * <p>
   * Checks existence, permission, and updates authorization details.
   * <p>
   * Logs modification activity.
   */
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

  /**
   * Enable or disable authorization control for a functional test plan.
   * <p>
   * Checks existence and permission before updating authorization status.
   * <p>
   * Logs enable/disable activity.
   */
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

  /**
   * Delete an authorization for a functional test plan.
   * <p>
   * Checks existence and permission before deleting authorization.
   * <p>
   * Logs cancel activity and deletes the authorization.
   */
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

  /**
   * Add creator authorization for a functional test plan.
   * <p>
   * Batch inserts creator authorizations for the specified plan.
   */
  @Override
  public void addCreatorAuth(Long planId, Set<Long> creatorIds) {
    batchInsert(toFuncPlanAuths(creatorIds, planId, FuncPlanPermission.ALL, true));
  }

  /**
   * Add owner and tester authorization for a functional test plan.
   * <p>
   * Batch inserts owner and tester authorizations for the specified plan.
   */
  @Override
  public void addOwnerAndTesterAuth(Long planId, Long ownerId, Set<Long> testerIds) {
    // Add owner auths
    if (nonNull(ownerId)) {
      batchInsert(toFuncPlanAuths(Set.of(ownerId), planId, FuncPlanPermission.ALL, false));
    }
    // Add tester auths
    testerIds.remove(getUserId());
    testerIds.remove(ownerId);
    if (isNotEmpty(testerIds)) {
      batchInsert(toFuncPlanAuths(testerIds, planId, FuncPlanPermission.TESTER, false));
    }
  }

  /**
   * Delete tester authorization by plan ID.
   * <p>
   * Removes tester authorizations for the specified plan and tester IDs.
   */
  @Override
  public void deleteAuthByPlanId(Long planId, Collection<Long> testerIds) {
    funcPlanAuthRepo.deleteByPlanIdAndAuthObjectId(planId, testerIds);
  }

  /**
   * Get the repository for functional test plan authorizations.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncPlanAuth, Long> getRepository() {
    return this.funcPlanAuthRepo;
  }
}
