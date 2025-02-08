package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @author xiaolong.liu
 */
public interface ScenarioTestFacade {

  void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled);

  List<TestType> testEnabledFind(Long scenarioId);

  ScenarioTestCount countProjectTestScenarios(Long projectId, OrgAndDateFilterDto dto);

  void testTaskGenerate(Long scenarioId, @Nullable Long taskSprintId,
      Set<ScenarioTestTaskGenerateDto> dto);

  void testTaskRestart(Long scenarioId);

  void testTaskReopen(Long scenarioId);

  void testTaskDelete(Long scenarioId, Set<TestType> testTypes);

  List<Server> serverList(Long scenarioId);

  void testExecAdd(Long scenarioId, List<Server> servers);

  TestResultDetailVo testResult(Long scenarioId);

}
