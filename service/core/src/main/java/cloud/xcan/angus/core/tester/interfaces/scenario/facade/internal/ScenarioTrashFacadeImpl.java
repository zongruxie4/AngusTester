package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioTrashAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTrashCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashSearch;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.trash.ScenarioTrashSearchDto;
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

  @Resource
  private ScenarioTrashSearch scenarioTrashSearch;

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
  public PageResult<ScenarioTrashDetailVo> search(ScenarioTrashSearchDto dto) {
    Page<ScenarioTrash> page = scenarioTrashSearch.search(getSearchCriteria(dto), dto.tranPage(),
        ScenarioTrash.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ScenarioTrashAssembler::toDetailVo);
  }

}




