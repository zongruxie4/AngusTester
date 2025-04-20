package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;
import cloud.xcan.angus.remote.PageResult;

public interface MockApisLogFacade {

  MockApisLogDetailVo detail(Long id);

  PageResult<MockApisLogListVo> list(Long mockServiceId, MockApisLogFindDto dto);

  PageResult<MockApisLogListVo> search(Long mockServiceId, MockApisLogSearchDto dto);

}
