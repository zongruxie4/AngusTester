package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface ScenarioMonitorCmd {

  IdKey<Long, Object> add(ScenarioMonitor monitor);

  void update(ScenarioMonitor monitor);

  IdKey<Long, Object> replace(ScenarioMonitor monitor);

  void runNow(Long id);

  void delete(Collection<Long> ids);

}
