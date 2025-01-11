package cloud.xcan.sdf.core.angustester.interfaces.activity.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.vo.ActivityDetailVo;

public interface ActivityFacade {

  PageResult<ActivityDetailVo> list(ActivityFindDto dto);

  PageResult<ActivityDetailVo> search(ActivitySearchDto dto);
}
