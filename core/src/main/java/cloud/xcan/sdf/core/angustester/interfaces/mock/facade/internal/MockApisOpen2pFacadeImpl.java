package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisOpen2pFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisAssembler;
import cloud.xcan.sdf.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.sdf.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.sdf.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.sdf.model.remoting.vo.MockApisServiceInfoVo;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class MockApisOpen2pFacadeImpl implements MockApisOpen2pFacade {

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockApisCmd mockApisCmd;

  @Override
  public MockApisServiceInfoVo mockService(Long id) {
    MockService mockService = mockServiceQuery.info0(id);
    return isNull(mockService) ? null : MockApisAssembler.toMockServiceDetailVo(mockService);
  }

  @Override
  public List<MockApisInfoVo> mockApis(MockApisDetailDto dto) {
    List<MockApis> mockApis = mockApisQuery.info0(dto.getMockServiceId(),
        dto.getMethod(), dto.getEndpoint());
    return isEmpty(mockApis) ? null : mockApis.stream().map(MockApisAssembler::toMockApisDetailVo)
        .collect(Collectors.toList());
  }

  @Override
  public void counterUpdate(MockApisRequestCountDto dto) {
    mockApisCmd.counterUpdate(dto.getApisCounter0());
  }

}
