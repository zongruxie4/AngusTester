package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler.toMockApisServiceInfoVo;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisOpen2pFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler;
import cloud.xcan.angus.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.angus.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.angus.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisServiceInfoVo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
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
  public void counterUpdate(MockApisRequestCountDto dto) {
    mockApisCmd.counterUpdate(dto.getApisCounter0());
  }

  @Override
  public MockApisServiceInfoVo mockService(Long id) {
    MockService mockService = mockServiceQuery.info0(id);
    return isNull(mockService) ? null : toMockApisServiceInfoVo(mockService);
  }

  @Override
  public List<MockApisInfoVo> mockApis(MockApisDetailDto dto) {
    List<MockApis> mockApis = mockApisQuery.info0(dto.getMockServiceId(),
        dto.getMethod(), dto.getEndpoint());
    return isEmpty(mockApis) ? null : mockApis.stream()
        .map(MockApisAssembler::toMockApisDetailVo).toList();
  }

}
