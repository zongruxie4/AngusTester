package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Set;

public interface ScenarioAuthCmd {

  IdKey<Long, Object> add(ScenarioAuth scenarioAuth);

  void replace(ScenarioAuth scenarioAuth);

  void delete(Long scenarioId);

  void enabled(Long projectId, Boolean enabled);

  void addCreatorAuth(Set<Long> creatorIds, Long scenarioId);

  void moveCreatorAuth(Set<Long> creatorIds, Long scenarioId);

}




