package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncBaselineCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional baseline cases.
 * </p>
 * <p>
 * Provides methods for adding, establishing, and deleting baseline cases for functional testing.
 * Handles permission checks, baseline status validation, and version management.
 * </p>
 * <p>
 * Key features include baseline case association, establishment workflow, and version
 * increment management for functional test cases.
 * </p>
 */
@Biz
public class FuncBaselineCaseCmdImpl extends CommCmd<FuncBaselineCase, Long> implements
    FuncBaselineCaseCmd {

  @Resource
  private FuncBaselineCaseRepo funcBaselineCaseRepo;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncBaselineCmd funcBaselineCmd;
  @Resource
  private FuncBaselineQuery funcBaselineQuery;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  /**
   * <p>
   * Add cases to a functional baseline.
   * </p>
   * <p>
   * Checks baseline existence, status, plan consistency, and permission. Adds case IDs to the baseline.
   * Validates that the baseline is not established and ensures proper authorization.
   * </p>
   * @param baselineId the ID of the baseline
   * @param caseIds the set of case IDs to add
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void add(Long baselineId, HashSet<Long> caseIds) {
    new BizTemplate<Void>() {
      FuncBaseline funcBaselineDb;

      @Override
      protected void checkParams() {
        // Validate baseline exists and retrieve details
        funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);

        // Ensure baseline is not already established (only draft baselines can be modified)
        ProtocolAssert.assertTrue(!funcBaselineDb.getEstablished(),
            "The established baseline does not allow adding use cases. Please create a new baseline and try again");

        // Verify case IDs are consistent with the baseline's plan
        funcPlanQuery.checkConsistent(funcBaselineDb.getPlanId(), caseIds);

        // Validate user has permission to establish baseline for this plan
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), funcBaselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Add case IDs to baseline and update
        funcBaselineDb.getCaseIds().addAll(caseIds);
        funcBaselineCmd.update0(funcBaselineDb);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Establish a functional baseline from given cases.
   * </p>
   * <p>
   * Inserts baseline cases and increments version for the specified case IDs.
   * Creates baseline case records and updates case versions for tracking.
   * </p>
   * @param caseIds the set of case IDs to establish as baseline
   * @param baselineCases the list of baseline case details
   */
  @Override
  public void establishBaseline(Set<Long> caseIds, List<FuncBaselineCase> baselineCases) {
    // Insert baseline case records
    batchInsert0(baselineCases);
    // Increment version for all cases to track baseline establishment
    funcCaseRepo.incrVersionByIdIn(caseIds);
  }

  /**
   * <p>
   * Delete cases from a functional baseline.
   * </p>
   * <p>
   * Checks baseline existence, status, plan consistency, and permission. Removes case IDs from the baseline.
   * Validates that the baseline is not established and ensures proper authorization.
   * </p>
   * @param baselineId the ID of the baseline
   * @param caseIds the set of case IDs to delete
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long baselineId, HashSet<Long> caseIds) {
    new BizTemplate<Void>() {
      FuncBaseline funcBaselineDb;

      @Override
      protected void checkParams() {
        // Validate baseline exists and retrieve details
        funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);

        // Ensure baseline is not already established (only draft baselines can be modified)
        ProtocolAssert.assertTrue(!funcBaselineDb.getEstablished(),
            "The established baseline does not allow deleting use cases. ");

        // Verify case IDs are consistent with the baseline's plan
        funcPlanQuery.checkConsistent(funcBaselineDb.getPlanId(), caseIds);

        // Validate user has permission to establish baseline for this plan
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), funcBaselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Remove case IDs from baseline and update
        funcBaselineDb.getCaseIds().removeAll(caseIds);
        funcBaselineCmd.update0(funcBaselineDb);
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for FuncBaselineCase entity.
   * <p>
   * @return the FuncBaselineCaseRepo instance
   */
  @Override
  protected BaseRepository<FuncBaselineCase, Long> getRepository() {
    return funcBaselineCaseRepo;
  }
}
