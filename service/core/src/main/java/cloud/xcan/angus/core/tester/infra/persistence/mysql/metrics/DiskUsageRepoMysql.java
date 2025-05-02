package cloud.xcan.angus.core.tester.infra.persistence.mysql.metrics;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsageRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface DiskUsageRepoMysql extends DiskUsageRepo {

}
