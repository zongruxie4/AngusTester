package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_PLAN;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_STATUS_MISMATCH_T;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus.BLOCKED;
import static cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus.COMPLETED;
import static cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus.IN_PROGRESS;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCmd;
import cloud.xcan.angus.core.tester.application.converter.FuncPlanConverter;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrashRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional test plans.
 * </p>
 * <p>
 * Provides methods for adding, updating, replacing, starting, ending, blocking, cloning, and deleting test plans.
 * Handles permission checks, plan lifecycle management, authorization setup, and activity logging.
 * </p>
 * <p>
 * Key features include plan lifecycle management, authorization initialization,
 * result and review reset functionality, and comprehensive activity tracking.
 * </p>
 */
@Biz
public class FuncPlanCmdImpl extends CommCmd<FuncPlan, Long> implements FuncPlanCmd {

  @Resource
  private FuncPlanRepo funcPlanRepo;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanAuthCmd funcPlanAuthCmd;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseCmd funcCaseCmd;
  @Resource
  private FuncReviewQuery funcReviewQuery;
  @Resource
  private FuncReviewCmd funcReviewCmd;
  @Resource
  private FuncTrashRepo trashFuncRepo;
  @Resource
  private UserManager userManager;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Add a new functional test plan.
   * </p>
   * <p>
   * Checks project membership, plan name, date range, quota, owner, and testers before adding.
   * Initializes plan creator, owner, and tester authorizations, and logs creation activity.
   * </p>
   * @param plan the plan entity to add
   * @return ID and name of the created plan
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncPlan plan) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Set<Long> testerIds;

      @Override
      protected void checkParams() {
        // Validate user is a member of the project
        projectMemberQuery.checkMember(getUserId(), plan.getProjectId());

        // Check if plan name already exists in the project
        funcPlanQuery.checkNameExists(plan.getProjectId(), plan.getName());

        // Check plan date range validation
        // NOOP:: funcPlanQuery.checkPlanDateRange(plan.getStartDate(), plan.getDeadlineDate());

        // Validate quota limits
        funcPlanQuery.checkQuota();

        // Verify owner exists
        userManager.checkAndFind(plan.getOwnerId());

        // Verify all testers exist
        testerIds = plan.getTesterResponsibilities().keySet();
        userManager.checkAndFind(testerIds);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Insert the plan and get ID
        IdKey<Long, Object> idKey = insert(plan);

        // Initialize plan creator authorization
        Long currentUserId = getUserId();
        funcPlanAuthCmd.addCreatorAuth(idKey.getId(), Set.of(currentUserId));

        // Initialize plan owner and tester authorizations
        funcPlanAuthCmd.addOwnerAndTesterAuth(idKey.getId(),
            Objects.equals(plan.getOwnerId(), currentUserId) ? null : plan.getOwnerId(), testerIds);

        // Log plan creation activity
        activityCmd.add(toActivity(FUNC_PLAN, plan, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Update an existing functional test plan.
   * <p>
   * Checks existence, name, date range, permission, owner, and testers before updating.
   * <p>
   * Updates plan details, authorizations, and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(FuncPlan plan) {
    new BizTemplate<Void>() {
      FuncPlan planDb;
      Set<Long> testerIds;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(plan.getId());
        // Check the plan name exists
        if (isNotEmpty(plan.getName()) && !planDb.getName().equals(plan.getName())) {
          funcPlanQuery.checkNameExists(planDb.getProjectId(), plan.getName());
        }

        // Check the plan date range
        // Fix:: The time of modification can not be a future value.
        // if (nonNull(plan.getStartDate()) && nonNull(plan.getEndDate())) {
        //   funcPlanQuery.checkPlanDateRange(plan.getStartDate(), plan.getEndDate());
        // }

        // Check the dir permission
        funcPlanAuthQuery.checkModifyPlanAuth(getUserId(), planDb.getId());
        // Check the owner exists
        if (nonNull(plan.getOwnerId())) {
          userManager.checkAndFind(plan.getOwnerId());
        }
        // Check the testers exists
        if (isNotEmpty(plan.getTesterResponsibilities())) {
          testerIds = plan.getTesterResponsibilities().keySet();
          userManager.checkAndFind(testerIds);
        }
      }

      @Override
      protected Void process() {
        if (nonNull(plan.getEvalWorkloadMethod())
            && !plan.getEvalWorkloadMethod().equals(planDb.getEvalWorkloadMethod())) {
          funcCaseRepo.updateEvalWorkloadMethodByPlanId(plan.getId(), plan.getEvalWorkloadMethod());
        }

        // Clear case review status when closing review
        if (!Objects.equals(planDb.getReview(), plan.getReview())) {
          updateCaseReviewStatus(plan, planDb);
        }

        funcPlanRepo.save(copyPropertiesIgnoreNull(plan, planDb));

        if (isNotEmpty(plan.getTesterResponsibilities())) {
          replacePlanAuths(planDb, testerIds);
        }

        activityCmd.add(toActivity(FUNC_PLAN, planDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replace (add or update) a functional test plan.
   * <p>
   * Adds a new plan or updates an existing one, handling authorizations and logging activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(FuncPlan plan) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;
      Set<Long> testerIds;

      @Override
      protected void checkParams() {
        if (nonNull(plan.getId())) {
          // Check the plan exists
          planDb = funcPlanQuery.checkAndFind(plan.getId());

          // Check the plan name exists
          if (!planDb.getName().equals(plan.getName())) {
            funcPlanQuery.checkNameExists(planDb.getProjectId(), plan.getName());
          }

          // Check the plan date range
          // Fix:: The time of modification can not be a future value.
          // funcPlanQuery.checkPlanDateRange(plan.getStartDate(), plan.getEndDate());

          // Check the dir permission
          funcPlanAuthQuery.checkModifyPlanAuth(getUserId(), planDb.getId());
          // Check the owner exists
          userManager.checkAndFind(plan.getOwnerId());
          // Check the testers exists
          testerIds = plan.getTesterResponsibilities().keySet();
          userManager.checkAndFind(testerIds);
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(plan.getId())) {
          return add(plan);
        }

        if (nonNull(plan.getEvalWorkloadMethod())
            && !plan.getEvalWorkloadMethod().equals(planDb.getEvalWorkloadMethod())) {
          funcCaseRepo.updateEvalWorkloadMethodByPlanId(plan.getId(), plan.getEvalWorkloadMethod());
        }

        // Clear case review status when closing review
        if (!Objects.equals(planDb.getReview(), plan.getReview())) {
          updateCaseReviewStatus(plan, planDb);
        }

        FuncPlanConverter.setReplaceInfo(plan, planDb);
        funcPlanRepo.save(plan);

        replacePlanAuths(planDb, testerIds);

        activityCmd.add(toActivity(FUNC_PLAN, planDb, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(planDb.getId()).setKey(planDb.getName());
      }
    }.execute();
  }

  /**
   * Start a functional test plan.
   * <p>
   * Checks existence, permission, and status before starting the plan.
   * <p>
   * Logs status update activity.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(Long id) {
    new BizTemplate<Void>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(id);

        // Check the testing permission
        funcPlanAuthQuery.checkModifyPlanAuth(getUserId(), planDb.getId());

        // Check the status is allowed
        assertTrue(planDb.getStatus().allowStart(), PLAN_STATUS_MISMATCH_T,
            new Object[]{planDb.getStatus(), IN_PROGRESS});
      }

      @Override
      protected Void process() {
        planDb.setStatus(IN_PROGRESS);
        funcPlanRepo.save(planDb);

        activityCmd.add(toActivity(FUNC_PLAN, planDb, STATUS_UPDATE, IN_PROGRESS));
        return null;
      }
    }.execute();
  }

  /**
   * End a functional test plan.
   * <p>
   * Checks existence, permission, status, and case completion before ending the plan.
   * <p>
   * Logs status update activity.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void end(Long id) {
    new BizTemplate<Void>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(id);

        // Check the testing permission
        funcPlanAuthQuery.checkModifyPlanAuth(getUserId(), planDb.getId());

        // Check the status is allowed
        assertTrue(planDb.getStatus().allowEnd(), PLAN_STATUS_MISMATCH_T,
            new Object[]{planDb.getStatus(), COMPLETED});

        // Check the cases is completed
        funcPlanQuery.checkPlanCaseCompleted(id);
      }

      @Override
      protected Void process() {
        planDb.setStatus(COMPLETED);
        funcPlanRepo.save(planDb);

        activityCmd.add(toActivity(FUNC_PLAN, planDb, STATUS_UPDATE, COMPLETED));
        return null;
      }
    }.execute();
  }

  /**
   * Block a functional test plan.
   * <p>
   * Checks existence, permission, and status before blocking the plan.
   * <p>
   * Logs status update activity.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void block(Long id) {
    new BizTemplate<Void>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(id);

        // Check the testing permission
        funcPlanAuthQuery.checkModifyPlanAuth(getUserId(), planDb.getId());

        // Check the status is allowed
        assertTrue(planDb.getStatus().allowBlock(), PLAN_STATUS_MISMATCH_T,
            new Object[]{planDb.getStatus(), BLOCKED});
      }

      @Override
      protected Void process() {
        planDb.setStatus(BLOCKED);
        funcPlanRepo.save(planDb);

        activityCmd.add(toActivity(FUNC_PLAN, planDb, STATUS_UPDATE, BLOCKED));
        return null;
      }
    }.execute();
  }

  /**
   * Clone a functional test plan.
   * <p>
   * Checks existence and project membership before cloning.
   * <p>
   * Logs clone activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(id);

        // Check the project member
        projectMemberQuery.checkMember(getUserId(), planDb.getProjectId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        FuncPlan newPlan = FuncPlanConverter.clone(planDb);
        funcPlanQuery.setSafeCloneName(newPlan);
        IdKey<Long, Object> idKey = insert(newPlan, "name");

        activityCmd.add(toActivity(FUNC_PLAN, planDb, ActivityType.CLONE, planDb.getName()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Resets test results for a batch of functional test plans.
   * <p>
   * Validates plan existence and user permissions before resetting test results.
   * <p>
   * Resets all case test results to initial state and restarts completed plans.
   * <p>
   * Logs reset activities for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncPlan> plansDb;

      @Override
      protected void checkParams() {
        // Ensure all plans exist in database
        plansDb = funcPlanQuery.checkAndFind(ids);

        // Check user permission to reset test results
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getProjectId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_TEST_RESULT);
      }

      @Override
      protected Void process() {
        // Reset all case test results to initial state
        funcCaseRepo.updateTestResultToInitByPlanIds(ids);

        // Restart plans that were completed
        startPlanIfCompleted(plansDb);

        // Log test result reset activities for audit
        activityCmd.addAll(toActivities(FUNC_PLAN, plansDb, ActivityType.RESULT_RESET));
        return null;
      }
    }.execute();
  }

  /**
   * Resets review results for a batch of functional test plans.
   * <p>
   * Validates plan existence and user permissions before resetting review results.
   * <p>
   * Resets all case review results and associated review sessions.
   * <p>
   * Logs reset activities and restarts completed plans if needed.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncPlan> plansDb;

      @Override
      protected void checkParams() {
        // Ensure all plans exist in database
        plansDb = funcPlanQuery.checkAndFind(ids);

        // Check user permission to reset review results
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getProjectId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_REVIEW_RESULT);
      }

      @Override
      protected Void process() {
        // Reset all case review results to initial state
        funcCaseRepo.updateReviewResultToInitByPlanIds(ids);

        // Reset associated review sessions
        Set<Long> reviewIds = funcReviewQuery.findByPlanIds(ids)
            .stream().map(FuncReview::getPlanId).collect(Collectors.toSet());
        if (isNotEmpty(reviewIds)) {
          funcReviewCmd.reviewReset(reviewIds, true);
        }

        // Restart plans that were completed
        startPlanIfCompleted(plansDb);

        // Log review result reset activities for audit
        activityCmd.addAll(toActivities(FUNC_PLAN, plansDb, ActivityType.RESULT_RESET));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes a functional test plan (soft delete).
   * <p>
   * Validates plan existence and user permissions before marking the plan as deleted.
   * <p>
   * Moves plan to trash for potential recovery and updates associated case delete status.
   * <p>
   * Logs deletion activity for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<FuncPlan> planDb;

      @Override
      protected void checkParams() {
        // Check if plan exists in database
        planDb = funcPlanRepo.findById(id);
        if (planDb.isEmpty()) {
          return;
        }

        // Check user permission to delete the plan
        funcPlanAuthQuery.checkDeletePlanAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (planDb.isEmpty()) {
          return null;
        }

        FuncPlan plan0 = planDb.get();

        // Mark plan as deleted (soft delete)
        plan0.setDeleted(true).setDeletedBy(getUserId()).setDeletedDate(LocalDateTime.now());
        funcPlanRepo.save(plan0);

        // Update associated cases delete status
        funcCaseRepo.updatePlanDeleteStatusByPlan(singleton(id), true);

        // Move plan to trash for potential recovery
        trashFuncRepo.save(FuncPlanConverter.toFuncTrash(plan0));

        // Log plan deletion activity for audit
        activityCmd.add(toActivity(FUNC_PLAN, plan0, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Permanently deletes all data related to a batch of functional test plans.
   * <p>
   * Removes plans, associated cases, and reviews from the database.
   * <p>
   * This operation is irreversible and should be used with caution.
   */
  @Override
  public void delete0(List<Long> ids) {
    // Permanently delete all plans
    funcPlanRepo.deleteAllByIdIn(ids);

    // Get all case IDs associated with these plans
    List<Long> caseIds = funcCaseRepo.findAll0IdByPlanIdIn(ids);
    if (isNotEmpty(caseIds)) {
      // Permanently delete all associated cases
      funcCaseCmd.delete0(caseIds);
    }

    // Permanently delete all associated reviews
    funcReviewCmd.deleteByPlanId(ids);
  }

  /**
   * Updates case review status based on plan review setting changes.
   * <p>
   * When review is disabled, clears all case review results.
   * <p>
   * When review is enabled, sets all case review results to started state.
   */
  private void updateCaseReviewStatus(FuncPlan plan, FuncPlan planDb) {
    if (Objects.equals(false, plan.getReview())) {
      // Clear review results when review is disabled
      funcCaseRepo.updateReviewResultToDisabledByPlanId(plan.getId());
    } else {
      // Set review results to started when review is enabled
      funcCaseRepo.updateReviewResultToStartedByPlanId(plan.getId());
    }
  }

  /**
   * Replaces plan authorizations for updated testers.
   * <p>
   * Removes existing authorizations and creates new ones for plan creator,
   * owner, and testers.
   */
  private void replacePlanAuths(FuncPlan planDb, Set<Long> testerIds) {
    // Collect all user IDs that need authorization changes
    Set<Long> allTesterIds = new HashSet<>(testerIds);
    allTesterIds.add(planDb.getCreatedBy());
    allTesterIds.add(planDb.getOwnerId());

    // Remove existing authorizations for all users
    funcPlanAuthCmd.deleteAuthByPlanId(planDb.getId(), allTesterIds);

    // Initialize authorization for plan creator
    funcPlanAuthCmd.addCreatorAuth(planDb.getId(), Set.of(planDb.getCreatedBy()));

    // Initialize authorization for plan owner and testers (excluding creator)
    Set<Long> safeTesterIds = new HashSet<>(testerIds);
    safeTesterIds.remove(planDb.getCreatedBy());
    funcPlanAuthCmd.addOwnerAndTesterAuth(planDb.getId(), planDb.getOwnerId(), safeTesterIds);
  }

  /**
   * Restarts completed plans by setting their status to PENDING.
   * <p>
   * Used when test results are reset to allow testing to continue.
   */
  private void startPlanIfCompleted(List<FuncPlan> plansDb) {
    // Find all plans that are currently completed
    List<FuncPlan> completedPlansDb = plansDb.stream()
        .filter(x -> x.getStatus().isCompleted()).toList();

    if (isNotEmpty(completedPlansDb)) {
      // Set all completed plans back to PENDING status
      for (FuncPlan plan : completedPlansDb) {
        plan.setStatus(FuncPlanStatus.PENDING);
      }
      funcPlanRepo.saveAll(completedPlansDb);
    }
  }

  /**
   * Get the repository for functional test plans.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncPlan, Long> getRepository() {
    return this.funcPlanRepo;
  }
}
