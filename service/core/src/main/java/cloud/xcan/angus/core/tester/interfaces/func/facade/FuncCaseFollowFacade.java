package cloud.xcan.angus.core.tester.interfaces.func.facade;

import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFollowFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFollowDetailVo> search(FuncCaseFollowSearchDto dto);

  Long count(Long projectId);
}
