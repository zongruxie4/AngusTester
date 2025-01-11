package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ScenarioFavouriteFacade {

  IdKey<Long, Object> add(Long scenarioId);

  void cancel(Long scenarioId);

  void cancelAll(Long projectId);

  PageResult<ScenarioFavouriteDetailVo> search(ScenarioFavouriteFindDto dto);

  Long count(Long projectId);

}
