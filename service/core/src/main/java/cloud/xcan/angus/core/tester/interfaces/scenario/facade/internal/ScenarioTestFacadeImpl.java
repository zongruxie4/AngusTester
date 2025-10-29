package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioTestAssembler.generateToTask;

import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTestCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskTestFacade;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
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
  private ExecResultFacade execResultFacade;

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
      Set<ScenarioTestTaskGenerateDto> dto) {
    scenarioTestCmd.testTaskGenerate(scenarioId, taskSprintId, generateToTask(scenarioId, dto));
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
    result.setTestResult(execResultFacade.scenarioResult(scenarioId));
    result.setAssocTasks(taskTestFacade.assocList(TaskType.SCENARIO_TEST, scenarioId));
    return result;
  }

}
