package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;

public interface ScenarioMonitorHistoryCmd {

  ScenarioMonitorHistory run(ScenarioMonitor monitor);

}
