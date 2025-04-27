package cloud.xcan.angus.core.tester.interfaces.func.facade;

import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFavouriteFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFavouriteDetailVo> search(FuncCaseFavouriteSearchDto dto);

  Long count(Long projectId);
}
