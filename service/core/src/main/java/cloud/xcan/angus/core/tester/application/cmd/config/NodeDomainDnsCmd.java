package cloud.xcan.angus.core.tester.application.cmd.config;

import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDns;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainDnsCmd {

  IdKey<Long, Object> add(NodeDomainDns nodeDomainDns);

  void update(NodeDomainDns nodeDomainDns);

  void delete(Long id);

}




