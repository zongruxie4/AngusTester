package cloud.xcan.angus.core.tester.application.cmd.node;

import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainDnsCmd {

  IdKey<Long, Object> add(NodeDomainDns nodeDomainDns);

  void update(NodeDomainDns nodeDomainDns);

  void delete(Long id);

}




