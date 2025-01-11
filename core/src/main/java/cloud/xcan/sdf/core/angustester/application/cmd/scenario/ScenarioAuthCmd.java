package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Set;

public interface ScenarioAuthCmd {

  IdKey<Long, Object> add(ScenarioAuth scenarioAuth);

  void replace(ScenarioAuth scenarioAuth);

  void delete(Long scenarioId);

  void enabled(Long projectId, Boolean enabledFlag);

  void addCreatorAuth(Set<Long> creatorIds, Long scenarioId);

  void moveCreatorAuth(Set<Long> creatorIds, Long scenarioId);

}




