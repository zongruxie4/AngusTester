package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomainRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeDomainRepo")
public interface NodeDomainRepoMysql extends NodeDomainRepo {


}
