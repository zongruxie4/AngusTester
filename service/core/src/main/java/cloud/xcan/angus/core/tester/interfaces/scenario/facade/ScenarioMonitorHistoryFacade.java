package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import cloud.xcan.angus.remote.PageResult;

public interface ScenarioMonitorHistoryFacade {

  ScenarioMonitorHistoryDetailVo detail(Long id);

  PageResult<ScenarioMonitorHistoryListVo> list(ScenarioMonitorHistoryFindDto dto);

}
