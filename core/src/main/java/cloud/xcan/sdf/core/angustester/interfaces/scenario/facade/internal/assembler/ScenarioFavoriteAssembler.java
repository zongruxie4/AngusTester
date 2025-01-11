package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;

public class ScenarioFavoriteAssembler {

  public static ScenarioFavourite addDtoToDomain(Long scenarioId) {
    return new ScenarioFavourite().setScenarioId(scenarioId);
  }

  public static ScenarioFavouriteDetailVo toDetailVo(ScenarioFavourite favorite) {
    return new ScenarioFavouriteDetailVo().setId(favorite.getId())
        .setScenarioId(favorite.getScenarioId())
        .setScenarioName(favorite.getScenarioName())
        .setPlugin(favorite.getPlugin());
  }

}




