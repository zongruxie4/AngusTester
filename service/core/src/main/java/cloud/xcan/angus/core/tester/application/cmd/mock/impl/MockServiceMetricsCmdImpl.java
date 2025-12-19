package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceMetricsCmd;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import org.springframework.stereotype.Service;

/**
 * Command implementation for mock service metrics management.
 * <p>
 * Provides methods for deleting JVM service usage metrics by service IDs.
 * <p>
 * Ensures batch deletion and repository access.
 */
@Service
public class MockServiceMetricsCmdImpl implements MockServiceMetricsCmd {

  @Resource
  private JvmServiceUsageRepo jvmServiceUsageRepo;

  /**
   * Delete JVM service usage metrics by service IDs.
   * <p>
   * Removes all metrics data associated with the given service IDs.
   */
  @Override
  public void deleteByServiceIds(Collection<Long> serviceIds) {
    jvmServiceUsageRepo.deleteByIdIn(serviceIds);
  }

}
