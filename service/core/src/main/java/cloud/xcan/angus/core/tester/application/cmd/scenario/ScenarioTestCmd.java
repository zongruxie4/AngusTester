package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;

public interface ScenarioTestCmd {

  void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled);

  void testExecAdd(Long scenarioId, List<Server> servers);

}
