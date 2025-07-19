package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorHistoryAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorHistoryAssembler.toDetail;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorHistoryFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorHistoryAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMonitorHistoryFacadeImpl implements ScenarioMonitorHistoryFacade {

  @Resource
  private ScenarioMonitorHistoryQuery scenarioMonitorHistoryQuery;

  @NameJoin
  @Override
  public ScenarioMonitorHistoryDetailVo detail(Long id) {
    return toDetail(scenarioMonitorHistoryQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorHistoryListVo> list(ScenarioMonitorHistoryFindDto dto) {
    Page<ScenarioMonitorHistoryInfo> page = scenarioMonitorHistoryQuery
        .list(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ScenarioMonitorHistoryAssembler::toListVo);
  }
}
