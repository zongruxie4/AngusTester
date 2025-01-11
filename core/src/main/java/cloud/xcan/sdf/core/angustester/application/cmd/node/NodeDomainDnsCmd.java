package cloud.xcan.sdf.core.angustester.application.cmd.node;

import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDns;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface NodeDomainDnsCmd {

  IdKey<Long, Object> add(NodeDomainDns nodeDomainDns);

  void update(NodeDomainDns nodeDomainDns);

  void delete(Long id);

}




