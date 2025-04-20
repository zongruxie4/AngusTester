package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;

public interface ScenarioTestQuery {

  List<TestType> findEnabledTestTypes(Long scenarioId);

  ScenarioTestCount countProjectTestScenario(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  TestScenarioCount countTestResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  List<Server> findServers(Long scenarioId);

}
