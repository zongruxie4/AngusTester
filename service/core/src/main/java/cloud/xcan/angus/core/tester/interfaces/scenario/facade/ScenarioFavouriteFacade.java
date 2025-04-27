package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ScenarioFavouriteFacade {

  IdKey<Long, Object> add(Long scenarioId);

  void cancel(Long scenarioId);

  void cancelAll(Long projectId);

  PageResult<ScenarioFavouriteDetailVo> search(ScenarioFavouriteFindDto dto);

  Long count(Long projectId);

}
