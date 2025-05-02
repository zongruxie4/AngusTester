package cloud.xcan.angus.core.tester.infra.persistence.mysql.metrics;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface JvmServiceUsageRepoMysql extends JvmServiceUsageRepo {

}
