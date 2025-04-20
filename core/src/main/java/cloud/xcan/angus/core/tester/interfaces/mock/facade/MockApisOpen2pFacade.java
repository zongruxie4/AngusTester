package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.angus.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.angus.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisServiceInfoVo;
import java.util.List;

public interface MockApisOpen2pFacade {

  MockApisServiceInfoVo mockService(Long id);

  List<MockApisInfoVo> mockApis(MockApisDetailDto dto);

  void counterUpdate(MockApisRequestCountDto dto);
}
