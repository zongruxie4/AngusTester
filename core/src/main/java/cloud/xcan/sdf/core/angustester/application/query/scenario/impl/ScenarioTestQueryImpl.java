package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import static cloud.xcan.angus.core.utils.AngusUtils.toServer;
import static cloud.xcan.sdf.core.angustester.application.converter.ScenarioTestConverter.assembleScenarioTestCount;
import static cloud.xcan.sdf.core.angustester.application.converter.ScenarioTestConverter.assembleTestScenarioCount;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

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
        if (nonNull(scenarioDb.getTestFuncFlag()) && scenarioDb.getTestFuncFlag()) {
          enabledTestTypes.add(TestType.FUNCTIONAL);
        }
        if (nonNull(scenarioDb.getTestPerfFlag()) && scenarioDb.getTestPerfFlag()) {
          enabledTestTypes.add(TestType.PERFORMANCE);
        }
        if (nonNull(scenarioDb.getTestStabilityFlag()) && scenarioDb.getTestStabilityFlag()) {
          enabledTestTypes.add(TestType.STABILITY);
        }
        return enabledTestTypes;
      }
    }.execute();
  }

  @Override
  public ScenarioTestCount countProjectTestScenario(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ScenarioTestCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  @Override
  public TestScenarioCount countTestResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<TestScenarioCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
