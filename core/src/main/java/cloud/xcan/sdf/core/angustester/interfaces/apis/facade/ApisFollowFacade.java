package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.follow.ApisFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ApisFollowFacade {

  IdKey<Long, Object> add(Long apiId);

  void cancel(Long apisId);

  void cancelAll(Long projectId);

  PageResult<ApisFollowDetailVo> search(ApisFollowSearchDto dto);

  Long count(Long projectId);
}
