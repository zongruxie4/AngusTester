package cloud.xcan.angus.core.tester.application.cmd.config;

import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSetting;
import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import java.util.LinkedHashMap;
import java.util.List;

public interface SettingTenantCmd {

  void proxyReplace(ServerApiProxy apiProxy);

  void testerEventReplace(List<TesterEvent> testerEvent);

  void evaluationReplace(LinkedHashMap<EvaluationPurpose, Integer> evaluation);

  void funcReplace(FuncData func);

  void perfReplace(PerfData perf);

  void stabilityReplace(StabilityData stability);

  TenantSetting init(Long tenantId);

}
