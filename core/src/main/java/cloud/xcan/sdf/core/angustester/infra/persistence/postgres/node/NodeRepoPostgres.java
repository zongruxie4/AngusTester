package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.node;

import cloud.xcan.sdf.core.angustester.domain.node.NodeRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeRepo")
public interface NodeRepoPostgres extends NodeRepo {


}
