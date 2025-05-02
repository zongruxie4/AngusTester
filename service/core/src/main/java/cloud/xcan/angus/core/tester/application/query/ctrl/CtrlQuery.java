package cloud.xcan.angus.core.tester.application.query.ctrl;

import cloud.xcan.angus.remoting.common.node.DiscoveryNodeDto;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import java.util.List;

public interface CtrlQuery {

  DiscoveryNodeVo discovery(DiscoveryNodeDto dto);

  List<ChannelRouter> getConnections();
}
