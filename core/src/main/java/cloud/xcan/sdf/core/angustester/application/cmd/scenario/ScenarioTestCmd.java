package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;

public interface ScenarioTestCmd {

  void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled);

  void testTaskGenerate(Long scenarioId, Long taskSprintId, List<Task> tasks);

  void testTaskRetest(Long scenarioId, Boolean restartFlag);

  void testTaskDelete(Long scenarioId, Set<TestType> testTypes);

  void testExecAdd(Long scenarioId, List<Server> servers);

}
