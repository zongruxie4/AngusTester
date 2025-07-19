package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.favourite.ApisFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ApisFavouriteFacade {

  IdKey<Long, Object> add(Long apisId);

  void cancel(Long apiId);

  void cancelAll(Long projectId);

  PageResult<ApisFavouriteDetailVo> list(ApisFavouriteFindDto dto);

  Long count(Long projectId);
}
