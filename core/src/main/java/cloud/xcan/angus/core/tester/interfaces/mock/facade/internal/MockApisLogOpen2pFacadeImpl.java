package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import cloud.xcan.angus.api.pojo.ApisRequestLog;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisLogCmd;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisLogOpen2pFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisLogAssembler;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
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
