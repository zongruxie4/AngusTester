package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.toAddDomain;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.toReplaceDomain;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler.toUpdateDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioMonitorCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorSearch;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorSearchDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioMonitorAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
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
    return scenarioMonitorCmd.add(toAddDomain(dto));
  }

  @Override
  public void update(ScenarioMonitorUpdateDto dto) {
    scenarioMonitorCmd.update(toUpdateDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(ScenarioMonitorReplaceDto dto) {
    return scenarioMonitorCmd.replace(toReplaceDomain(dto));
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
    return toDetailVo(scenarioMonitorQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorListVo> list(ScenarioMonitorFindDto dto) {
    Page<ScenarioMonitor> page = scenarioMonitorQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ScenarioMonitorAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<ScenarioMonitorListVo> search(ScenarioMonitorSearchDto dto) {
    Page<ScenarioMonitor> page = scenarioMonitorSearch.search(getSearchCriteria(dto),
        dto.tranPage(), ScenarioMonitor.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ScenarioMonitorAssembler::toListVo);
  }
}
