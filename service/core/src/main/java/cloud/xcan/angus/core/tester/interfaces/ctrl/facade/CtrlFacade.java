package cloud.xcan.angus.core.tester.interfaces.ctrl.facade;

import cloud.xcan.angus.remoting.common.node.DiscoveryNodeDto;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import java.util.List;

public interface CtrlFacade {

  DiscoveryNodeVo discovery(DiscoveryNodeDto dto);

  List<ChannelRouter> connectionsInfo();

}
