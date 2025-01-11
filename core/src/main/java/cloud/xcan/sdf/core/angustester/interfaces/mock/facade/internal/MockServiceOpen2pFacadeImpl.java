package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockServiceOpen2pFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockServiceAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceInfoVo;
import java.util.Objects;
import javax.annotation.Resource;
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
