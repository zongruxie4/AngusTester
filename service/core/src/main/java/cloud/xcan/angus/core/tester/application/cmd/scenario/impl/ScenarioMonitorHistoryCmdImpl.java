package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_SCE_MONITOR_HISTORY_NUM;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorHistoryConverter.assembleScenarioMonitorResultInfo;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorHistoryConverter.readExecutionLogFromRemote;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecDebugCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioMonitorHistoryCmd;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Biz
public class ScenarioMonitorHistoryCmdImpl extends CommCmd<ScenarioMonitorHistory, Long>
    implements ScenarioMonitorHistoryCmd {

  @Resource
  private ScenarioMonitorHistoryRepo scenarioMonitorHistoryRepo;

  @Resource
  private ExecDebugCmd execDebugCmd;

  @Transactional(rollbackOn = Exception.class)
  @Override
  public ScenarioMonitorHistory run(ScenarioMonitor monitor) {
    ScenarioMonitorHistory history = new ScenarioMonitorHistory();
    history.setProjectId(monitor.getProjectId())
        .setMonitorId(monitor.getId()).setCreatedBy(getUserId());;
    try {
      ExecDebug result = execDebugCmd.startByMonitor(true, null, monitor.getId(),
          monitor.getScenarioId(), monitor.getScriptId(), ScriptType.TEST_FUNCTIONALITY,
          null, new Arguments(), monitor.getServerSetting());
      assembleScenarioMonitorResultInfo(history, result);
      try {
        readExecutionLogFromRemote(result, history);
      } catch (Throwable e) {
        log.warn("Exception in querying scenario monitoring execution logs: {}",
            e.getMessage());
      }
    } catch (Exception e) {
      history.setStatus(ScenarioMonitorStatus.FAILURE)
          .setFailureMessage("Execution scenario monitoring exception: " + e.getMessage());
    }
    insert0(history);

    // Maximum retention of historical records
    scenarioMonitorHistoryRepo.deleteHistory(monitor.getId(), MAX_SCE_MONITOR_HISTORY_NUM);
    return history;
  }

  @Override
  protected BaseRepository<ScenarioMonitorHistory, Long> getRepository() {
    return scenarioMonitorHistoryRepo;
  }

}
