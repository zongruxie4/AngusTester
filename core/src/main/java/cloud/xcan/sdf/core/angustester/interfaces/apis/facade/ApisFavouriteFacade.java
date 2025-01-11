package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.favourite.ApisFavouriteSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ApisFavouriteFacade {

  IdKey<Long, Object> add(Long apisId);

  void cancel(Long apiId);

  void cancelAll(Long projectId);

  PageResult<ApisFavouriteDetailVo> search(ApisFavouriteSearchDto dto);

  Long count(Long projectId);
}
