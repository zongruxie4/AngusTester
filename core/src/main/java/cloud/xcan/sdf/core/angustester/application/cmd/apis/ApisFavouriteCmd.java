package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ApisFavouriteCmd {

  IdKey<Long, Object> add(ApisFavourite favourite);

  void cancel(Long apisId);

  void cancelAll(Long projectId);

}
