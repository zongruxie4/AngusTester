package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ScenarioCmd {

  IdKey<Long, Object> add(Scenario scenario);

  void update(Scenario scenario);

  IdKey<Long, Object> replace(Scenario scenario);

  void move(Long scenarioId, Long targetDirId);

  IdKey<Long, Object> clone(Long id);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Long id);

  void delete0(List<Long> ids);

}




