package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.node;

import cloud.xcan.sdf.core.angustester.domain.node.NodeRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeRepo")
public interface NodeRepoMysql extends NodeRepo {


}
