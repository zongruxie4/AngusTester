package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioFavouriteCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioFavouriteQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioFavoriteAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
    return scenarioFavouriteCmd.add(ScenarioFavoriteAssembler.addDtoToDomain(scenarioId));
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
  public PageResult<ScenarioFavouriteDetailVo> search(ScenarioFavouriteFindDto dto) {
    Page<ScenarioFavourite> pageResult = scenarioFavouriteQuery
        .search(dto.getProjectId(), dto.getScenarioName(), dto.tranPage());
    return buildVoPageResult(pageResult, ScenarioFavoriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return scenarioFavouriteQuery.count(projectId);
  }
}




