package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioMonitorCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorSearch;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioMonitorFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorListVo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;

@Biz
public class ScenarioMonitorFacadeImpl implements ScenarioMonitorFacade {

  @Resource
  private ScenarioMonitorQuery scenarioMonitorQuery;

  @Resource
  private ScenarioMonitorSearch scenarioMonitorSearch;

  @Resource
  private ScenarioMonitorCmd scenarioMonitorCmd;

  @Override
  public IdKey<Long, Object> add(ScenarioMonitorAddDto dto) {
    ScenarioMonitor monitor = ScenarioMonitorAssembler.toAddDomain(dto);
    return scenarioMonitorCmd.add(monitor);
  }

  @Override
  public void update(ScenarioMonitorUpdateDto dto) {
    ScenarioMonitor monitor = ScenarioMonitorAssembler.toUpdateDomain(dto);
    scenarioMonitorCmd.update(monitor);
  }

  @Override
  public IdKey<Long, Object> replace(ScenarioMonitorReplaceDto dto) {
    ScenarioMonitor monitor = ScenarioMonitorAssembler.toReplaceDomain(dto);
    return scenarioMonitorCmd.replace(monitor);
  }

  @Override
  public void runNow(Long id) {
    scenarioMonitorCmd.runNow(id);
  }

  @Override
  public void delete(Collection<Long> ids) {
    scenarioMonitorCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ScenarioMonitorDetailVo detail(Long id) {
    ScenarioMonitor monitor = scenarioMonitorQuery.detail(id);
    return ScenarioMonitorAssembler.toDetailVo(monitor);
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorListVo> list(ScenarioMonitorFindDto dto) {
    Page<ScenarioMonitor> page = scenarioMonitorQuery
        .find(ScenarioMonitorAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ScenarioMonitorAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorListVo> search(ScenarioMonitorSearchDto dto) {
    Page<ScenarioMonitor> apisPage = scenarioMonitorSearch
        .search(ScenarioMonitorAssembler.getSearchCriteria(dto), dto.tranPage(),
            ScenarioMonitor.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(apisPage, ScenarioMonitorAssembler::toListVo);
  }
}
