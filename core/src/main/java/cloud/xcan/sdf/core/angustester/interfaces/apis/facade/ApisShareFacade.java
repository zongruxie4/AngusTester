package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareVo;
import java.util.Collection;

public interface ApisShareFacade {

  ApisShareAddVo add(ApisShareAddDto dto);

  void update(ApisShareUpdateDto dto);

  void delete(Collection<Long> ids);

  ApisShareVo detail(Long id);

  PageResult<ApisShareVo> list(ApisShareFindDto dto);

  PageResult<ApisShareVo> search(ApisShareSearchDto dto);

  ApisShareViewVo view(ApisShareViewDto dto);

}
