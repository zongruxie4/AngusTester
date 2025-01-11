package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface FuncCaseFollowFacade {

  IdKey<Long, Object> add(Long caseId);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

  PageResult<FuncCaseFollowDetailVo> search(FuncCaseFollowSearchDto dto);

  Long count(Long projectId);
}
