package cloud.xcan.angus.core.tester.application.query.config;

import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDns;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NodeDomainDnsQuery {

  NodeDomainDns find(Long id);

  Page<NodeDomainDns> find(Specification<NodeDomainDns> spec, Pageable pageable);

  NodeDomainDns checkAndFind(Long id);

  void checkAddNameExists(Long domainId, String name);

  void checkUpdateNameExists(Long id, Long domainId, String name);
}




