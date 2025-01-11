package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface FuncCaseFavouriteFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFavouriteDetailVo> search(FuncCaseFavouriteSearchDto dto);

  Long count(Long projectId);
}
