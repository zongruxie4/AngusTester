package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.favourite.FuncCaseFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFavouriteFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFavouriteDetailVo> list(FuncCaseFavouriteFindDto dto);

  Long count(Long projectId);
}
