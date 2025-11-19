package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseScenarioQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseScenario;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseScenarioRepo;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query implementation for functional test case scenario association queries.
 * <p>
 * Provides query operations for retrieving scenarios associated with functional test cases.
 */
@Biz
public class FuncCaseScenarioQueryImpl implements FuncCaseScenarioQuery {

  @Resource
  private FuncCaseScenarioRepo funcCaseScenarioRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  /**
   * Find all scenarios associated with a specific test case.
   * <p>
   * Retrieves scenario associations and returns the corresponding scenario objects.
   *
   * @param caseId the test case ID
   * @return List of scenarios associated with the test case
   */
  @Override
  public List<Scenario> findScenariosByCaseId(Long caseId) {
    return new BizTemplate<List<Scenario>>() {
      @Override
      protected List<Scenario> process() {
        // Find all scenario associations for the case
        List<FuncCaseScenario> associations = funcCaseScenarioRepo.findByCaseId(caseId);

        if (isEmpty(associations)) {
          return Collections.emptyList();
        }

        // Extract scenario IDs
        Set<Long> scenarioIds = associations.stream()
            .map(FuncCaseScenario::getScenarioId)
            .collect(Collectors.toSet());

        // Retrieve scenarios by IDs
        List<Scenario> scenario = scenarioQuery.findByIds(scenarioIds);

        // Set exec infos
        scenarioQuery.setExecInfo(scenario);
        return scenario;
      }
    }.execute();
  }
}

