package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_PLAN;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_STATUS_MISMATCH_T;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus.BLOCKED;
import static cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus.COMPLETED;
import static cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus.IN_PROGRESS;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
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
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrashRepo;
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncPlan plan) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Set<Long> testerIds;

      @Override
      protected void checkParams() {
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), plan.getProjectId());
        // Check the plan name exists
        funcPlanQuery.checkNameExists(plan.getProjectId(), plan.getName());
        // Check the plan date range
        // NOOP:: funcPlanQuery.checkPlanDateRange(plan.getStartDate(), plan.getDeadlineDate());
        // Check the quota limit
        funcPlanQuery.checkQuota();
        // Check the owner exists
        userManager.checkAndFind(plan.getOwnerId());
        // Check the testers exists
        testerIds = plan.getTesterResponsibilities().keySet();
        userManager.checkAndFind(testerIds);
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(plan);

        // Init plan creator auth
        Long currentUserId = getUserId();
        funcPlanAuthCmd.addCreatorAuth(idKey.getId(), Set.of(currentUserId));
        // Init plan owner and tester auth
        funcPlanAuthCmd.addOwnerAndTesterAuth(idKey.getId(),
            Objects.equals(plan.getOwnerId(), currentUserId) ? null : plan.getOwnerId(), testerIds);

        activityCmd.add(toActivity(FUNC_PLAN, plan, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

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
        ProtocolAssert.assertTrue(planDb.getStatus().allowStart(), PLAN_STATUS_MISMATCH_T,
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
        ProtocolAssert.assertTrue(planDb.getStatus().allowEnd(), PLAN_STATUS_MISMATCH_T,
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
        ProtocolAssert.assertTrue(planDb.getStatus().allowBlock(), PLAN_STATUS_MISMATCH_T,
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
   * Note: Manually modifying the results of API testing cases is also permitted.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncPlan> plansDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        plansDb = funcPlanQuery.checkAndFind(ids);

        // Check the test permission
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getProjectId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_TEST_RESULT);
      }

      @Override
      protected Void process() {
        funcCaseRepo.updateTestResultToInitByPlanIds(ids);

        startPlanIfCompleted(plansDb);

        activityCmd.addAll(toActivities(FUNC_PLAN, plansDb, ActivityType.RESULT_RESET));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncPlan> plansDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        plansDb = funcPlanQuery.checkAndFind(ids);

        // Check the test permission
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getProjectId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_REVIEW_RESULT);
      }

      @Override
      protected Void process() {
        funcCaseRepo.updateReviewResultToInitByPlanIds(ids);

        Set<Long> reviewIds = funcReviewQuery.findByPlanIds(ids)
            .stream().map(FuncReview::getPlanId).collect(Collectors.toSet());
        if (isNotEmpty(reviewIds)) {
          funcReviewCmd.reviewReset(reviewIds, true);
        }

        startPlanIfCompleted(plansDb);

        activityCmd.addAll(toActivities(FUNC_PLAN, plansDb, ActivityType.RESULT_RESET));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<FuncPlan> planDb;

      @Override
      protected void checkParams() {
        // Check the plan existed and authed
        planDb = funcPlanRepo.findById(id);
        if (planDb.isEmpty()) {
          return;
        }
        funcPlanAuthQuery.checkDeletePlanAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (planDb.isEmpty()) {
          return null;
        }

        FuncPlan plan0 = planDb.get();

        // Logic delete
        plan0.setDeleted(true).setDeletedBy(getUserId()).setDeletedDate(LocalDateTime.now());
        funcPlanRepo.save(plan0);

        // Fix:: Do not delete the case and cases after deleting the plan.
        funcCaseRepo.updatePlanDeleteStatusByPlan(singleton(id), true);

        // Add plan to Trash
        trashFuncRepo.save(FuncPlanConverter.toFuncTrash(plan0));

        // Add delete plan activity
        activityCmd.add(toActivity(FUNC_PLAN, plan0, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void delete0(List<Long> ids) {
    // Delete plan
    funcPlanRepo.deleteAllByIdIn(ids);

    List<Long> caseIds = funcCaseRepo.findAll0IdByPlanIdIn(ids);
    if (isNotEmpty(caseIds)) {
      funcCaseCmd.delete0(caseIds);
    }

    funcReviewCmd.deleteByPlanId(ids);
  }

  private void updateCaseReviewStatus(FuncPlan plan, FuncPlan planDb) {
    if (Objects.equals(false, plan.getReview())) {
      funcCaseRepo.updateReviewResultToDisabledByPlanId(plan.getId());
    } else {
      funcCaseRepo.updateReviewResultToStartedByPlanId(plan.getId());
    }
  }

  private void replacePlanAuths(FuncPlan planDb, Set<Long> testerIds) {
    // Delete tester auth
    Set<Long> allTesterIds = new HashSet<>(testerIds);
    allTesterIds.add(planDb.getCreatedBy());
    allTesterIds.add(planDb.getOwnerId());
    funcPlanAuthCmd.deleteAuthByPlanId(planDb.getId(), allTesterIds);
    // Init plan Creator auth
    funcPlanAuthCmd.addCreatorAuth(planDb.getId(), Set.of(planDb.getCreatedBy()));
    // Init plan owner and tester auth
    Set<Long> safeTesterIds = new HashSet<>(testerIds);
    safeTesterIds.remove(planDb.getCreatedBy());
    funcPlanAuthCmd.addOwnerAndTesterAuth(planDb.getId(), planDb.getOwnerId(), safeTesterIds);
  }

  private void startPlanIfCompleted(List<FuncPlan> plansDb) {
    List<FuncPlan> completedPlansDb = plansDb.stream()
        .filter(x -> x.getStatus().isCompleted()).collect(Collectors.toList());
    if (isNotEmpty(completedPlansDb)) {
      for (FuncPlan plan : completedPlansDb) {
        plan.setStatus(FuncPlanStatus.PENDING);
      }
      funcPlanRepo.saveAll(completedPlansDb);
    }
  }

  @Override
  protected BaseRepository<FuncPlan, Long> getRepository() {
    return this.funcPlanRepo;
  }
}
