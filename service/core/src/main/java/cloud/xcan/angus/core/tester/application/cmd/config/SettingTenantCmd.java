package cloud.xcan.angus.core.tester.application.cmd.config;

import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;
import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import java.util.List;

public interface SettingTenantCmd {

  void proxyReplace(ServerApiProxy apiProxy);

  void testerEventReplace(List<TesterEvent> testerEvent);

  void funcReplace(FuncData func);

  void perfReplace(PerfData perf);

  void stabilityReplace(StabilityData stability);

  SettingTenant init(Long tenantId);

}
