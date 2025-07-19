package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareVo;
import cloud.xcan.angus.remote.PageResult;
import java.util.Collection;

public interface ApisShareFacade {

  ApisShareAddVo add(ApisShareAddDto dto);

  void update(ApisShareUpdateDto dto);

  void delete(Collection<Long> ids);

  ApisShareVo detail(Long id);

  PageResult<ApisShareVo> list(ApisShareFindDto dto);

  ApisShareViewVo view(ApisShareViewDto dto);

}
