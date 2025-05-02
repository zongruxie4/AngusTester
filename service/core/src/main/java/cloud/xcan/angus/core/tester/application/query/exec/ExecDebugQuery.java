package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;

public interface ExecDebugQuery {

  ExecDebug scriptDetail(Long scriptId);

  ExecDebug scenarioDetail(Long scenarioId);

  ExecDebug monitorDetail(Long id);

  ExecDebug findByScriptId(Long id);

  ExecDebug findByScenarioId(Long id);

  ExecDebug findByMonitorId(Long monitorId);

  ExecDebug checkAndFind(Long id);

  void checkAddQuota(long incr);

  void checkConcurrentTaskQuota(long incr);

  void setDebugResult(ExecDebug debugDb);

}
