package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_SCENARIO;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_SCENARIO_CANCEL;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseScenarioCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseScenario;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseScenarioRepo;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for functional test case scenario association operations.
 * <p>
 * Provides operations for associating and canceling scenario associations with functional test
 * cases.
 * <p>
 * Implements business logic validation, permission checks, activity logging, and transaction
 * management for all scenario association operations.
 * <p>
 * Supports batch operations and enforces maximum association limits (20 scenarios per case).
 */
@Service
public class FuncCaseScenarioCmdImpl extends CommCmd<FuncCaseScenario, Long> implements
    FuncCaseScenarioCmd {

  private static final int MAX_SCENARIO_ASSOCIATIONS = 20;

  @Resource
  private FuncCaseScenarioRepo funcCaseScenarioRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add associated scenarios to a functional test case.
   * <p>
   * Checks permission and existence before associating scenarios and logging the activity.
   * <p>
   * Enforces maximum limit of 20 scenarios per case.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scenarioAssocAdd(Long caseId, HashSet<Long> scenarioIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<Scenario> assocScenariosDb;
      List<FuncCaseScenario> existingAssociations;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(caseId);

        // Ensure all associated scenarios exist
        assocScenariosDb = scenarioQuery.findByIds(scenarioIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseId);

        // Check existing associations
        existingAssociations = funcCaseScenarioRepo.findByCaseId(caseId);

        // Check if adding these scenarios would exceed the limit
        Set<Long> existingScenarioIds = existingAssociations.stream()
            .map(FuncCaseScenario::getScenarioId)
            .collect(Collectors.toSet());

        // Filter out already associated scenarios
        Set<Long> newScenarioIds = scenarioIds.stream()
            .filter(id -> !existingScenarioIds.contains(id))
            .collect(Collectors.toSet());

        int totalAfterAdd = existingScenarioIds.size() + newScenarioIds.size();
        if (totalAfterAdd > MAX_SCENARIO_ASSOCIATIONS) {
          throw QuotaException.of(
              String.format(
                  "用例最多只能关联 %d 个场景，当前已有 %d 个，尝试添加 %d 个，总计将超过限制",
                  MAX_SCENARIO_ASSOCIATIONS, existingScenarioIds.size(), newScenarioIds.size()));
        }
      }

      @Override
      protected Void process() {
        // Get existing scenario IDs to avoid duplicates
        Set<Long> existingScenarioIds = existingAssociations.stream()
            .map(FuncCaseScenario::getScenarioId)
            .collect(Collectors.toSet());

        // Create new associations for scenarios not already associated
        List<FuncCaseScenario> newAssociations = scenarioIds.stream()
            .filter(id -> !existingScenarioIds.contains(id))
            .map(scenarioId -> {
              FuncCaseScenario association = new FuncCaseScenario();
              association.setCaseId(caseId);
              association.setScenarioId(scenarioId);
              return association;
            })
            .collect(Collectors.toList());

        if (isNotEmpty(newAssociations)) {
          batchInsert(newAssociations);
        }

        // Log scenario association activity
        List<Scenario> newScenarios = assocScenariosDb.stream()
            .filter(s -> !existingScenarioIds.contains(s.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(newScenarios)) {
          Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_SCENARIO,
              newScenarios.stream().map(Scenario::getName).collect(Collectors.joining(",")));
          activityCmd.add(activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Cancel associated scenarios from a functional test case.
   * <p>
   * Checks permission and existence before removing scenario associations and logging the
   * activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scenarioAssocCancel(Long caseId, HashSet<Long> scenarioIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<Scenario> assocScenariosDb;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(caseId);

        // Ensure all associated scenarios exist
        assocScenariosDb = scenarioQuery.findByIds(scenarioIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseId);
      }

      @Override
      protected Void process() {
        // Remove scenario associations from the case
        funcCaseScenarioRepo.deleteByCaseIdAndScenarioIdIn(caseId, scenarioIds);

        // Log scenario association cancellation activity
        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_SCENARIO_CANCEL,
            assocScenariosDb.stream().map(Scenario::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<FuncCaseScenario, Long> getRepository() {
    return this.funcCaseScenarioRepo;
  }
}

