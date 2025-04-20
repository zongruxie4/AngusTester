package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface ScenarioMonitorCmd {

  IdKey<Long, Object> add(ScenarioMonitor monitor);

  void update(ScenarioMonitor monitor);

  IdKey<Long, Object> replace(ScenarioMonitor monitor);

  void runNow(Long id);

  void delete(Collection<Long> ids);

}
