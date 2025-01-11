package cloud.xcan.sdf.core.angustester.infra.iaas;

import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDns;

/**
 * @author wjl
 */
public interface DnsClient {

  boolean addDomain(String name);

  String addDomainDns(String domainName, NodeDomainDns nodeDomainDns);

  void updateDomainDns(NodeDomainDns nodeDomainDns);

  void deleteDomainDns(String recordId);
}
