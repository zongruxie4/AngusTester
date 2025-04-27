package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ScenarioFollowCmd {

  IdKey<Long, Object> add(ScenarioFollow follow);

  void cancel(Long id);

  void cancelAll(Long projectId);

}




