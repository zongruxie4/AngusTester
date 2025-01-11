package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ScenarioFollowCmd {

  IdKey<Long, Object> add(ScenarioFollow follow);

  void cancel(Long id);

  void cancelAll(Long projectId);

}




