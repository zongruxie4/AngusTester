package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE_BASELINE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.FuncBaselineCaseConverter.toBaselineCases;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncBaselineCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.query.test.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional baselines.
 * </p>
 * <p>
 * Provides methods for adding, updating, replacing, establishing, and deleting baselines for functional testing.
 * Handles permission checks, baseline status validation, activity logging, and batch operations.
 * </p>
 * <p>
 * Key features include baseline lifecycle management, case association, establishment workflow,
 * and comprehensive activity tracking for audit purposes.
 * </p>
 */
@Biz
public class FuncBaselineCmdImpl extends CommCmd<FuncBaseline, Long> implements FuncBaselineCmd {

  @Resource
  private FuncBaselineRepo funcBaselineRepo;
  @Resource
  private FuncBaselineCaseRepo funcBaselineCaseRepo;
  @Resource
  private FuncBaselineCaseCmd funcBaselineCaseCmd;
  @Resource
  private FuncBaselineQuery funcBaselineQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Add a new functional baseline.
   * </p>
   * <p>
   * Checks plan existence, permission, and case consistency. Inserts the baseline and logs the creation activity.
   * Validates that the plan exists and user has proper authorization to establish baselines.
   * </p>
   * @param baseline the baseline entity to add
   * @return ID and name of the created baseline
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncBaseline baseline) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Validate plan exists and retrieve details
        planDb = funcPlanQuery.checkAndFind(baseline.getPlanId());

        // Verify user has permission to establish baseline for this plan
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baseline.getPlanId());

        // Ensure baseline case IDs are consistent with the plan
        funcPlanQuery.checkConsistent(baseline.getPlanId(), baseline.getCaseIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Set project ID from plan and insert baseline
        baseline.setProjectId(planDb.getProjectId());
        IdKey<Long, Object> idKey = insert(baseline);

        // Log baseline creation activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baseline, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * <p>
   * Update an existing functional baseline.
   * </p>
   * <p>
   * Checks baseline existence and permission. Updates the baseline and logs the update activity.
   * Validates that the baseline exists and user has proper authorization.
   * </p>
   * @param baseline the baseline entity with updated values
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(FuncBaseline baseline) {
    new BizTemplate<Void>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        // Validate baseline exists and retrieve details
        baselineDb = funcBaselineQuery.checkAndFind(baseline.getId());

        // Verify user has permission to establish baseline for this plan
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Update baseline with non-null properties and save
        funcBaselineRepo.save(CoreUtils.copyPropertiesIgnoreNull(baseline, baselineDb));

        // Log baseline update activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baselineDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Update a functional baseline without additional checks.
   * <p>
   * Directly saves the baseline entity.
   */
  @Override
  public void update0(FuncBaseline baseline) {
    funcBaselineRepo.save(baseline);
  }

  /**
   * <p>
   * Replace (add or update) a functional baseline.
   * </p>
   * <p>
   * Adds a new baseline if ID is null, otherwise updates the existing baseline.
   * Handles both creation and update scenarios with appropriate validation.
   * </p>
   * @param baseline the baseline entity to add or update
   * @return ID and name of the baseline
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(FuncBaseline baseline) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        if (nonNull(baseline.getId())) {
          // Validate existing baseline exists and retrieve details
          baselineDb = funcBaselineQuery.checkAndFind(baseline.getId());

          // Verify user has permission to establish baseline for this plan
          funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(baselineDb)) {
          // Create new baseline if ID is null
          return add(baseline);
        }

        // Update existing baseline name and description
        baselineDb.setName(baseline.getName()).setDescription(baseline.getDescription());
        funcBaselineRepo.save(baselineDb);

        // Log baseline update activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baselineDb, ActivityType.UPDATED));
        return new IdKey<>(baselineDb.getId(), baselineDb.getName());
      }
    }.execute();
  }

  /**
   * <p>
   * Establish a functional baseline.
   * </p>
   * <p>
   * Checks baseline existence, status, case non-emptiness, and permission. Inserts baseline cases, marks as established, and logs activities.
   * Converts draft baseline to established state with case version tracking.
   * </p>
   * @param id the baseline ID to establish
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void establish(Long id) {
    new BizTemplate<Void>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        // Validate baseline exists and retrieve details
        baselineDb = funcBaselineQuery.checkAndFind(id);

        // Ensure baseline has not been established yet
        ProtocolAssert.assertTrue(isNull(baselineDb.getEstablished())
            || !baselineDb.getEstablished(), "Baseline has been established");

        // Verify baseline has at least one case
        ProtocolAssert.assertTrue(isNotEmpty(baselineDb.getCaseIds()),
            "The baseline case cannot be empty");

        // Validate user has permission to establish baseline for this plan
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Retrieve all cases for the baseline
        List<FuncCase> casesDb = funcCaseRepo.findAllByIdIn(baselineDb.getCaseIds());
        if (isNotEmpty(casesDb)) {
          // Convert cases to baseline cases and establish
          List<FuncBaselineCase> baselineCases = toBaselineCases(id, casesDb);
          funcBaselineCaseCmd.establishBaseline(baselineDb.getCaseIds(), baselineCases);

          // Mark baseline as established and save
          baselineDb.setEstablished(true);
          funcBaselineRepo.save(baselineDb);

          // Log establish baseline activities for all cases
          activityCmd.addAll(toActivities(FUNC_CASE, casesDb, ActivityType.ESTABLISH_BASELINE));
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Delete a batch of functional baselines.
   * </p>
   * <p>
   * Checks baseline existence and permission. Deletes baselines and their cases, and logs delete activities.
   * Performs batch deletion with proper authorization validation.
   * </p>
   * @param ids collection of baseline IDs to delete
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncBaseline> baselinesDb;

      @Override
      protected void checkParams() {
        // Validate all baselines exist and retrieve details
        baselinesDb = funcBaselineQuery.checkAndFind(ids);

        // Verify user has permission to establish baseline for all plans
        funcPlanAuthQuery.batchCheckPermission(baselinesDb.stream().map(FuncBaseline::getPlanId)
            .collect(Collectors.toSet()), FuncPlanPermission.ESTABLISH_BASELINE);
      }

      @Override
      protected Void process() {
        // Delete baselines and their associated cases
        funcBaselineRepo.deleteByIdIn(ids);
        funcBaselineCaseRepo.deleteByBaselineIdIn(ids);

        // Log delete baseline activities
        activityCmd.addAll(toActivities(FUNC_CASE_BASELINE, baselinesDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for FuncBaseline entity.
   * <p>
   * @return the FuncBaselineRepo instance
   */
  @Override
  protected BaseRepository<FuncBaseline, Long> getRepository() {
    return funcBaselineRepo;
  }
}
