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
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for managing functional baseline cases.
 * <p>
 * Provides methods for adding, establishing, and deleting baseline cases for functional testing.
 * Handles permission checks, baseline status validation, and version management.
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
   * Add cases to a functional baseline.
   * <p>
   * Checks baseline existence, status, plan consistency, and permission. Adds case IDs to the baseline.
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
        // Check the baseline exists
        funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);

        // Check the baseline is not established
        ProtocolAssert.assertTrue(!funcBaselineDb.getEstablished(),
            "The established baseline does not allow adding use cases. Please create a new baseline and try again");

        // Check the cases and baseline plan is consistent
        funcPlanQuery.checkConsistent(funcBaselineDb.getPlanId(), caseIds);

        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), funcBaselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        funcBaselineDb.getCaseIds().addAll(caseIds);
        funcBaselineCmd.update0(funcBaselineDb);
        return null;
      }
    }.execute();
  }

  /**
   * Establish a functional baseline from given cases.
   * <p>
   * Inserts baseline cases and increments version for the specified case IDs.
   * @param caseIds the set of case IDs to establish as baseline
   * @param baselineCases the list of baseline case details
   */
  @Override
  public void establishBaseline(Set<Long> caseIds, List<FuncBaselineCase> baselineCases) {
    batchInsert0(baselineCases);
    funcCaseRepo.incrVersionByIdIn(caseIds);
  }

  /**
   * Delete cases from a functional baseline.
   * <p>
   * Checks baseline existence, status, plan consistency, and permission. Removes case IDs from the baseline.
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
        // Check the baseline exists
        funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);

        // Check the baseline is not established
        ProtocolAssert.assertTrue(!funcBaselineDb.getEstablished(),
            "The established baseline does not allow deleting use cases. ");

        // Check the cases and baseline plan is consistent
        funcPlanQuery.checkConsistent(funcBaselineDb.getPlanId(), caseIds);

        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), funcBaselineDb.getPlanId());
      }

      @Override
      protected Void process() {
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
