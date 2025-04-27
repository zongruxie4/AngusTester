package cloud.xcan.angus.core.tester.infra.iaas;

import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;

/**
 * @author wjl
 */
public interface DnsClient {

  boolean addDomain(String name);

  String addDomainDns(String domainName, NodeDomainDns nodeDomainDns);

  void updateDomainDns(NodeDomainDns nodeDomainDns);

  void deleteDomainDns(String recordId);
}
