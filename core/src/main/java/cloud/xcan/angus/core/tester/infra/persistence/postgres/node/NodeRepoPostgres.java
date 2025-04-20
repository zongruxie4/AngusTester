package cloud.xcan.angus.core.tester.infra.persistence.postgres.node;

import cloud.xcan.angus.core.tester.domain.node.NodeRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeRepo")
public interface NodeRepoPostgres extends NodeRepo {


}
