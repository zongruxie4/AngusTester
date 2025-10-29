package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.follow.FuncCaseFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.follow.FuncCaseFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFollowFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFollowDetailVo> list(FuncCaseFollowFindDto dto);

  Long count(Long projectId);
}
