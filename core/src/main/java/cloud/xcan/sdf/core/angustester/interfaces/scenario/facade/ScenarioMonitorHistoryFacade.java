package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;

public interface ScenarioMonitorHistoryFacade {

  ScenarioMonitorHistoryDetailVo detail(Long id);

  PageResult<ScenarioMonitorHistoryListVo> list(ScenarioMonitorHistoryFindDto dto);

}
