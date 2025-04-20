package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceOpen2pFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceInfoVo;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MockServiceOpen2pFacadeImpl implements MockServiceOpen2pFacade {

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Override
  public MockServiceInfoVo info(Long id) {
    MockService service = mockServiceQuery.info(id);
    return Objects.nonNull(service) ? MockServiceAssembler.toInfoVo(service) : null;
  }

}
