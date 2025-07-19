package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioTrashAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTrashCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashQuery;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.trash.ScenarioTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioTrashAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioTrashFacadeImpl implements ScenarioTrashFacade {

  @Resource
  private ScenarioTrashCmd scenarioTrashCmd;

  @Resource
  private ScenarioTrashQuery scenarioTrashQuery;

  @Override
  public void clear(Long id) {
    scenarioTrashCmd.clear(id);
  }

  @Override
  public void clearAll(Long projectId) {
    scenarioTrashCmd.clearAll(projectId);
  }

  @Override
  public void back(Long id) {
    scenarioTrashCmd.back(id);
  }

  @Override
  public void backAll(Long projectId) {
    scenarioTrashCmd.backAll(projectId);
  }

  @Override
  public Long count(Long projectId) {
    return scenarioTrashQuery.count(projectId);
  }

  @Override
  public PageResult<ScenarioTrashDetailVo> list(ScenarioTrashListDto dto) {
    Page<ScenarioTrash> page = scenarioTrashQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ScenarioTrashAssembler::toDetailVo);
  }

}




