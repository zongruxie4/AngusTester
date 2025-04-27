package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ApisFavouriteCmd {

  IdKey<Long, Object> add(ApisFavourite favourite);

  void cancel(Long apisId);

  void cancelAll(Long projectId);

}
