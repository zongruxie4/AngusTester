package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import cloud.xcan.sdf.api.pojo.ApisRequestLog;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisLogCmd;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisLogOpen2pFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisLogAssembler;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class MockApisLogOpen2pFacadeImpl implements MockApisLogOpen2pFacade {

  @Resource
  private MockApisLogCmd mockApisLogCmd;

  @Override
  public List<IdKey<Long, Object>> add(List<ApisRequestLog> dtos) {
    return dtos.stream().map(x -> mockApisLogCmd.add0(MockApisLogAssembler.toMockApisLog(x)))
        .collect(Collectors.toList());
  }

}
