package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ScenarioFavouriteCmd {

  IdKey<Long, Object> add(ScenarioFavourite favorite);

  void cancel(Long id);

  void cancelAll(Long projectId);
}




