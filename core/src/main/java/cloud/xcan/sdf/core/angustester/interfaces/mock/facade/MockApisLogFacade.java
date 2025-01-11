package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;

public interface MockApisLogFacade {

  MockApisLogDetailVo detail(Long id);

  PageResult<MockApisLogListVo> list(Long mockServiceId, MockApisLogFindDto dto);

  PageResult<MockApisLogListVo> search(Long mockServiceId, MockApisLogSearchDto dto);

}
