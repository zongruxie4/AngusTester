package cloud.xcan.angus.core.tester.interfaces.ctrl.facade.internal;

import cloud.xcan.angus.core.tester.application.query.ctrl.CtrlQuery;
import cloud.xcan.angus.core.tester.interfaces.ctrl.facade.CtrlFacade;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeDto;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CtrlFacadeImpl implements CtrlFacade {

  @Resource
  private CtrlQuery ctrlQuery;

  @Override
  public DiscoveryNodeVo discovery(DiscoveryNodeDto dto) {
    return ctrlQuery.discovery(dto);
  }

  @Override
  public List<ChannelRouter> connectionsInfo() {
    return ctrlQuery.getConnections();
  }

}
