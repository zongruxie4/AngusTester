package cloud.xcan.angus.core.tester.interfaces.config.facade;


import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.FuncTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.PerfTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.StabilityTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.TenantServerApiProxyTo;
import java.util.List;

public interface SettingTenantFacade {

  void proxyReplace(TenantServerApiProxyTo dto);

  TenantServerApiProxyTo proxyDetail();

  void testerEventReplace(List<TesterEvent> dto);

  List<TesterEvent> testerEventDetail();

  void funcReplace(FuncTo funcTo);

  FuncTo funcDetail();

  void perfReplace(PerfTo perfTo);

  PerfTo perfDetail();

  void stabilityReplace(StabilityTo stability);

  StabilityTo stabilityDetail();

}
