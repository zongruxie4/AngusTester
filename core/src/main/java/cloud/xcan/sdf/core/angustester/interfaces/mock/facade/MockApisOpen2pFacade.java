package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.sdf.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.sdf.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.sdf.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.sdf.model.remoting.vo.MockApisServiceInfoVo;
import java.util.List;

public interface MockApisOpen2pFacade {

  MockApisServiceInfoVo mockService(Long id);

  List<MockApisInfoVo> mockApis(MockApisDetailDto dto);

  void counterUpdate(MockApisRequestCountDto dto);
}
