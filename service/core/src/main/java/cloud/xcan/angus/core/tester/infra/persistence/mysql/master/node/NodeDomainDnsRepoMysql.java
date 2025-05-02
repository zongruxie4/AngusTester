package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.node;

import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDnsRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeDomainDnsRepo")
public interface NodeDomainDnsRepoMysql extends NodeDomainDnsRepo {


}
