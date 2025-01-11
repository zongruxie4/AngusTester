package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;

public interface ScenarioMonitorHistoryCmd {

  ScenarioMonitorHistory run(ScenarioMonitor monitor);

}
