package cloud.xcan.angus.core.tester.infra.persistence.postgres.metrics;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsageRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface DiskUsageRepoPostgres extends DiskUsageRepo {

}
