package cloud.xcan.sdf.core.angustester.application.cmd.node;

import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomain;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface NodeDomainCmd {

  IdKey<Long, Object> add(NodeDomain nodeDomain);

  void update(NodeDomain nodeDomain);

  void delete(Long id);

}




