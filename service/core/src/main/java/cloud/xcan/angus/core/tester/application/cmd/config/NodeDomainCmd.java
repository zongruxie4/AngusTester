package cloud.xcan.angus.core.tester.application.cmd.config;

import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainCmd {

  IdKey<Long, Object> add(NodeDomain nodeDomain);

  void update(NodeDomain nodeDomain);

  void delete(Long id);

}




