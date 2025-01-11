package cloud.xcan.sdf.core.angustester.application.cmd.node;

import cloud.xcan.sdf.core.angustester.domain.node.role.NodeRole;
import java.util.List;

public interface NodeRoleCmd {

  void add0(List<NodeRole> roles);

  void replace0(List<NodeRole> roles);
}
