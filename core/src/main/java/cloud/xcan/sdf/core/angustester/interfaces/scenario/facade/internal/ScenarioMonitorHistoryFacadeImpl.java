package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioMonitorHistoryFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorHistoryAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMonitorHistoryFacadeImpl implements ScenarioMonitorHistoryFacade {

  @Resource
  private ScenarioMonitorHistoryQuery scenarioMonitorHistoryQuery;

  @NameJoin
  @Override
  public ScenarioMonitorHistoryDetailVo detail(Long id) {
    ScenarioMonitorHistory history = scenarioMonitorHistoryQuery.detail(id);
    return ScenarioMonitorHistoryAssembler.toDetail(history);
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorHistoryListVo> list(ScenarioMonitorHistoryFindDto dto) {
    Page<ScenarioMonitorHistoryInfo> page = scenarioMonitorHistoryQuery
        .find(ScenarioMonitorHistoryAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ScenarioMonitorHistoryAssembler::toListVo);
  }
}
