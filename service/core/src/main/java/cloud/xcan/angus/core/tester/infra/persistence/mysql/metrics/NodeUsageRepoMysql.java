package cloud.xcan.angus.core.tester.infra.persistence.mysql.metrics;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsageRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeUsageRepoMysql extends NodeUsageRepo {

}
