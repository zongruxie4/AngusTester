package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;

/**
 * @author XiaoLong Liu
 */
public interface ScenarioTestFacade {

  void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled);

  List<TestType> testEnabledFind(Long scenarioId);

  ScenarioTestCount countProjectTestScenarios(Long projectId, OrgAndDateFilterDto dto);

  List<Server> serverList(Long scenarioId);

  void testExecAdd(Long scenarioId, List<Server> servers);

  TestResultDetailVo testResult(Long scenarioId);

}
