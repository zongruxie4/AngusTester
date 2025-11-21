package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import cloud.xcan.angus.core.tester.domain.config.node.NodeRepo;
import org.springframework.stereotype.Repository;

@Repository("nodeRepo")
public interface NodeRepoMysql extends NodeRepo {


}
