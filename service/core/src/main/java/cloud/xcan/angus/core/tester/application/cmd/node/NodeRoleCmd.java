package cloud.xcan.angus.core.tester.application.cmd.node;

import cloud.xcan.angus.core.tester.domain.node.role.NodeRole;
import java.util.List;

public interface NodeRoleCmd {

  void add0(List<NodeRole> roles);

  void replace0(List<NodeRole> roles);
}
