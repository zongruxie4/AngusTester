package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioTestAssembler.generateToTask;

import cloud.xcan.sdf.api.angusctrl.exec.ExecResultRemote;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioTestCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskTestFacade;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class ScenarioTestFacadeImpl implements ScenarioTestFacade {

  @Resource
  private ScenarioTestCmd scenarioTestCmd;

  @Resource
  private ScenarioTestQuery scenarioTestQuery;

  @Resource
  private TaskTestFacade taskTestFacade;

  @Resource
  private ExecResultRemote execResultRemote;

  @Override
  public void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled) {
    scenarioTestCmd.testEnabled(scenarioId, testTypes, enabled);
  }

  @Override
  public List<TestType> testEnabledFind(Long scenarioId) {
    return scenarioTestQuery.findEnabledTestTypes(scenarioId);
  }

  @Override
  public ScenarioTestCount countProjectTestScenarios(Long projectId, OrgAndDateFilterDto dto) {
    return scenarioTestQuery.countProjectTestScenario(projectId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public void testTaskGenerate(Long scenarioId, @Nullable Long taskSprintId,
      Set<ScenarioTestTaskGenerateDto> dtos) {
    scenarioTestCmd.testTaskGenerate(scenarioId, taskSprintId, generateToTask(scenarioId, dtos));
  }

  @Override
  public void testTaskRestart(Long scenarioId) {
    scenarioTestCmd.testTaskRetest(scenarioId, true);
  }

  @Override
  public void testTaskReopen(Long scenarioId) {
    scenarioTestCmd.testTaskRetest(scenarioId, false);
  }

  @Override
  public void testTaskDelete(Long scenarioId, Set<TestType> testTypes) {
    scenarioTestCmd.testTaskDelete(scenarioId, testTypes);
  }

  @Override
  public List<Server> serverList(Long scenarioId) {
    return scenarioTestQuery.findServers(scenarioId);
  }

  @Override
  public void testExecAdd(Long scenarioId, List<Server> servers) {
    scenarioTestCmd.testExecAdd(scenarioId, servers);
  }

  @Override
  public TestResultDetailVo testResult(Long scenarioId) {
    TestResultDetailVo result = new TestResultDetailVo();
    result.setTestResult(execResultRemote.scenarioResult(scenarioId).orElseContentThrow());
    result.setAssocTasks(taskTestFacade.assocList(TaskType.SCENARIO_TEST, scenarioId));
    return result;
  }

}
