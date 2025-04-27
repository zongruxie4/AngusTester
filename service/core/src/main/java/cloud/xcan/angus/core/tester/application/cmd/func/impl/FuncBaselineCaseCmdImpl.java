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

  @Override
  public void establishBaseline(Set<Long> caseIds, List<FuncBaselineCase> baselineCases) {
    batchInsert0(baselineCases);
    funcCaseRepo.incrVersionByIdIn(caseIds);
  }

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

  @Override
  protected BaseRepository<FuncBaselineCase, Long> getRepository() {
    return funcBaselineCaseRepo;
  }
}
