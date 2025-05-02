package cloud.xcan.angus.core.tester.infra.persistence.postgres.metrics;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsageRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeUsageRepoPostgres extends NodeUsageRepo {

}
