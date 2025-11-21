package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDnsRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeDomainDnsRepo")
public interface NodeDomainDnsRepoMysql extends NodeDomainDnsRepo {


}
