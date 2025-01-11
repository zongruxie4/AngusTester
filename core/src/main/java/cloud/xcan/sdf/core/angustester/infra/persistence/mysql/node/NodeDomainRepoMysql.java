package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.node;

import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomainRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeDomainRepo")
public interface NodeDomainRepoMysql extends NodeDomainRepo {


}
