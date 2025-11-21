package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.node;

import cloud.xcan.angus.core.tester.domain.config.node.NodeRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeRepo")
public interface NodeRepoPostgres extends NodeRepo {


}
