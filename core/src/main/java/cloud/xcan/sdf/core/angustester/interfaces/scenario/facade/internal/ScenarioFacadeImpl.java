package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioAssembler.addDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioAssembler.replaceDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioAssembler.updateDtoToDomain;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioListVo;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioSearch;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioAssembler;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioFacadeImpl implements ScenarioFacade {

  @Resource
  private ScenarioCmd scenarioCmd;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioSearch scenarioSearch;

  @Override
  public IdKey<Long, Object> add(ScenarioAddDto dto) {
    return scenarioCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(ScenarioUpdateDto dto) {
    scenarioCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(ScenarioReplaceDto dto) {
    return scenarioCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    scenarioCmd.delete(id);
  }

  @Override
  public void move(Long scenarioId, Long targetProjectId) {
    scenarioCmd.move(scenarioId, targetProjectId);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return scenarioCmd.clone(id);
  }

  @Override
  public List<IdKey<Long, Object>> sampleImport(Long projectId) {
    return scenarioCmd.sampleImport(projectId);
  }

  @NameJoin
  @Override
  public ScenarioDetailVo detail(Long id) {
    Scenario scenario = scenarioQuery.detail(id);
    return ScenarioAssembler.toDetailVo(scenario);
  }

  @NameJoin
  @Override
  public List<ScenarioListVo> list(Set<Long> ids) {
    return scenarioQuery.list(ids).stream().map(ScenarioAssembler::toListVo)
        .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public PageResult<ScenarioListVo> list(ScenarioInfoFindDto dto) {
    Page<Scenario> page = scenarioQuery
        .find(ScenarioAssembler.getSpecification(dto), dto.tranPage(), Scenario.class);
    return buildVoPageResult(page, ScenarioAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<ScenarioListVo> search(ScenarioInfoSearchDto dto) {
    Page<Scenario> apisPage = scenarioSearch
        .search(ScenarioAssembler.getSearchCriteria(dto), dto.tranPage(), Scenario.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(apisPage, ScenarioAssembler::toListVo);
  }

}
