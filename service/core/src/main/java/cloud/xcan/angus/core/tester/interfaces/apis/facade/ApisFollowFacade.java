package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.follow.ApisFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ApisFollowFacade {

  IdKey<Long, Object> add(Long apiId);

  void cancel(Long apisId);

  void cancelAll(Long projectId);

  PageResult<ApisFollowDetailVo> list(ApisFollowFindDto dto);

  Long count(Long projectId);
}
