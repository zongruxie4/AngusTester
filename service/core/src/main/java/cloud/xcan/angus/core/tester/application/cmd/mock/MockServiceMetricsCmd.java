package cloud.xcan.angus.core.tester.application.cmd.mock;

import java.util.Collection;

public interface MockServiceMetricsCmd {

  void deleteByServiceIds(Collection<Long> serviceIds);
}
