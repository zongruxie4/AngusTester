package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.node;

import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomainRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeDomainRepo")
public interface NodeDomainRepoMysql extends NodeDomainRepo {


}
