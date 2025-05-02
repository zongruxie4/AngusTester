package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceMetricsCmd;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import jakarta.annotation.Resource;
import java.util.Collection;

@Biz
public class MockServiceMetricsCmdImpl implements MockServiceMetricsCmd {

  @Resource
  private JvmServiceUsageRepo jvmServiceUsageRepo;

  @Override
  public void deleteByServiceIds(Collection<Long> serviceIds) {
    jvmServiceUsageRepo.deleteByIdIn(serviceIds);
  }

}
