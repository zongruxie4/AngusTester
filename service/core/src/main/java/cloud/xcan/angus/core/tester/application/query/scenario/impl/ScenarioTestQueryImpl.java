package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.core.tester.application.converter.ScenarioTestConverter.assembleScenarioTestCount;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioTestConverter.assembleTestScenarioCount;
import static cloud.xcan.angus.core.utils.AngusUtils.toServer;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

/**
 * <p>
 * Implementation of ScenarioTestQuery for scenario testing management and query operations.
 * </p>
 * <p>
 * Provides methods for test type management, test statistics, server discovery, and test result counting.
 * </p>
 */
@Biz
public class ScenarioTestQueryImpl implements ScenarioTestQuery {

  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Find enabled test types for a specific scenario.
   * </p>
   * <p>
   * Returns test types based on the scenario's test function configuration (functional, performance, stability).
   * </p>
   * @param scenarioId Scenario ID
   * @return List of enabled test types
   */
  @Override
  public List<TestType> findEnabledTestTypes(Long scenarioId) {
    return new BizTemplate<List<TestType>>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        scenarioDb = scenarioQuery.checkAndFind(scenarioId);
      }

      @Override
      protected List<TestType> process() {
        List<TestType> enabledTestTypes = new ArrayList<>();
        if (nonNull(scenarioDb.getTestFunc()) && scenarioDb.getTestFunc()) {
          enabledTestTypes.add(TestType.FUNCTIONAL);
        }
        if (nonNull(scenarioDb.getTestPerf()) && scenarioDb.getTestPerf()) {
          enabledTestTypes.add(TestType.PERFORMANCE);
        }
        if (nonNull(scenarioDb.getTestStability()) && scenarioDb.getTestStability()) {
          enabledTestTypes.add(TestType.STABILITY);
        }
        return enabledTestTypes;
      }
    }.execute();
  }

  /**
   * <p>
   * Count test scenarios in a project within a specified time range.
   * </p>
   * <p>
   * Supports filtering by creator object type and provides comprehensive test scenario statistics.
   * </p>
   * @param projectId Project ID
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId Creator object ID
   * @param createdDateStart Start date for filtering
   * @param createdDateEnd End date for filtering
   * @return Test scenario count statistics
   */
  @Override
  public ScenarioTestCount countProjectTestScenario(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ScenarioTestCount>() {

      @Override
      protected ScenarioTestCount process() {
        ScenarioTestCount count = new ScenarioTestCount();

        List<Scenario> scenarios = getProjectScenarios(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(scenarios)) {
          assembleScenarioTestCount(count, scenarios);
        }
        return count;
      }
    }.execute();
  }

  /**
   * <p>
   * Count test results for scenarios in a project within a specified time range.
   * </p>
   * <p>
   * Provides test result statistics including success, failure, and other test outcome metrics.
   * </p>
   * @param projectId Project ID
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId Creator object ID
   * @param createdDateStart Start date for filtering
   * @param createdDateEnd End date for filtering
   * @return Test result count statistics
   */
  @Override
  public TestScenarioCount countTestResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<TestScenarioCount>() {

      @Override
      protected TestScenarioCount process() {
        TestScenarioCount count = new TestScenarioCount();

        List<Scenario> scenarios = getProjectScenarios(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(scenarios)) {
          assembleTestScenarioCount(count, scenarios);
        }
        return count;
      }
    }.execute();
  }

  /**
   * <p>
   * Find servers used in a scenario by analyzing its script content.
   * </p>
   * <p>
   * Extracts server information from both configuration variables and HTTP pipeline elements,
   * removing duplicates based on server URLs.
   * </p>
   * @param scenarioId Scenario ID
   * @return List of unique servers
   */
  @Override
  public List<Server> findServers(Long scenarioId) {
    return new BizTemplate<List<Server>>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        scenarioDb = scenarioQuery.checkAndFind(scenarioId);
      }

      @Override
      protected List<Server> process() {
        Script script = scriptQuery.checkAndFind(scenarioDb.getScriptId());
        AngusScript angusScript = scriptQuery.checkAndParse(script.getContent(), false);
        if (isNull(angusScript)) {
          return emptyList();
        }
        List<Server> servers = new ArrayList<>();
        if (nonNull(angusScript.getConfiguration()) && isNotEmpty(
            angusScript.getConfiguration().getVariables())) {
          servers.addAll(angusScript.getConfiguration().getVariables().stream()
              .filter(x -> x.isExtractable() && x.getExtraction() instanceof HttpExtraction)
              .map(x -> (HttpExtraction) x.getExtraction())
              .filter(x -> nonNull(x.getRequest().getServer())
                  && nonNull(x.getRequest().getServer().getUrl()))
              .map(x -> toServer(x.getRequest().getServer()))
              .collect(Collectors.toList()));
        }
        if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
          servers.addAll(angusScript.getTask().getPipelines().stream().filter(
                  x -> x instanceof Http && nonNull(((Http) x).getRequest())
                      && nonNull(((Http) x).getRequest().getServer())
                      && nonNull(((Http) x).getRequest().getServer().getUrl()))
              .map(x -> toServer(((Http) x).getRequest().getServer()))
              .collect(Collectors.toList()));
        }
        return servers.stream().filter(ObjectUtils.distinctByKey(Server::getUrl))
            .collect(Collectors.toList());
      }
    }.execute();
  }

  /**
   * <p>
   * Get project scenarios with filtering by creator and date range.
   * </p>
   * <p>
   * Applies common deleted resource filters and sorts results by ID in descending order.
   * </p>
   * @param projectId Project ID
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId Creator object ID
   * @param createdDateStart Start date for filtering
   * @param createdDateEnd End date for filtering
   * @return List of filtered scenarios
   */
  private List<Scenario> getProjectScenarios(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    Set<Long> createdBys = null;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    }

    Set<SearchCriteria> allFilters = getCommonDeletedResourcesStatsFilter(projectId,
        createdDateStart, createdDateEnd, createdBys);
    return scenarioRepo.findAllByFilters(allFilters, Sort.by(Order.desc("id")));
  }
}
