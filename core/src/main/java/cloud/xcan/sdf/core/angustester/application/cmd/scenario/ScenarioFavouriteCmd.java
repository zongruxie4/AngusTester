package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ScenarioFavouriteCmd {

  IdKey<Long, Object> add(ScenarioFavourite favorite);

  void cancel(Long id);

  void cancelAll(Long projectId);
}




