package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioFavoriteAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioFavoriteAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioFavouriteFacadeImpl implements ScenarioFavouriteFacade {

  @Resource
  private ScenarioFavouriteCmd scenarioFavouriteCmd;

  @Resource
  private ScenarioFavouriteQuery scenarioFavouriteQuery;

  @Override
  public IdKey<Long, Object> add(Long scenarioId) {
    return scenarioFavouriteCmd.add(addDtoToDomain(scenarioId));
  }

  @Override
  public void cancel(Long scenarioId) {
    scenarioFavouriteCmd.cancel(scenarioId);
  }

  @Override
  public void cancelAll(Long projectId) {
    scenarioFavouriteCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<ScenarioFavouriteDetailVo> list(ScenarioFavouriteFindDto dto) {
    Page<ScenarioFavourite> page = scenarioFavouriteQuery
        .list(dto.getProjectId(), dto.getScenarioName(), dto.tranPage());
    return buildVoPageResult(page, ScenarioFavoriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return scenarioFavouriteQuery.count(projectId);
  }
}




