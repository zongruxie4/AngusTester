package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_SCE_MONITOR_HISTORY_NUM;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorHistoryConverter.assembleScenarioMonitorResultInfo;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorHistoryConverter.readExecutionLogFromRemote;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import org.springframework.stereotype.Service;
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

/**
 * Command implementation for scenario monitoring history management operations.
 * <p>
 * Provides execution and recording functionality for scenario monitoring histories.
 * <p>
 * Implements execution log retrieval, error handling, and history retention management.
 * <p>
 * Supports execution result tracking, log analysis, and historical data cleanup.
 */
@Slf4j
@Service
public class ScenarioMonitorHistoryCmdImpl extends CommCmd<ScenarioMonitorHistory, Long>
    implements ScenarioMonitorHistoryCmd {

  @Resource
  private ScenarioMonitorHistoryRepo scenarioMonitorHistoryRepo;
  @Resource
  private ExecDebugCmd execDebugCmd;

  /**
   * Executes a scenario monitor and records its execution history.
   * <p>
   * Runs the monitoring scenario using debug execution and captures execution results.
   * <p>
   * Retrieves execution logs from remote systems and handles execution failures gracefully.
   * <p>
   * Maintains history retention limits and ensures proper cleanup of old records.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public ScenarioMonitorHistory run(ScenarioMonitor monitor) {
    // Initialize history record with basic information
    ScenarioMonitorHistory history = new ScenarioMonitorHistory();
    history.setProjectId(monitor.getProjectId())
        .setMonitorId(monitor.getId()).setCreatedBy(getUserId());

    try {
      // Execute the monitoring scenario using debug execution
      ExecDebug result = execDebugCmd.startByMonitor(true, null, monitor.getId(),
          monitor.getScenarioId(), monitor.getScriptId(), ScriptType.TEST_FUNCTIONALITY,
          null, new Arguments(), monitor.getServerSetting());

      // Assemble execution results into history record
      assembleScenarioMonitorResultInfo(history, result);

      try {
        // Retrieve execution logs from remote system
        readExecutionLogFromRemote(result, history);
      } catch (Throwable e) {
        // Log warning but continue processing if log retrieval fails
        log.warn("Exception in querying scenario monitoring execution logs: {}",
            e.getMessage());
      }
    } catch (Exception e) {
      // Handle execution failures and record error information
      history.setStatus(ScenarioMonitorStatus.FAILURE)
          .setFailureMessage("Execution scenario monitoring exception: " + e.getMessage());
    }

    // Save history record to database
    insert0(history);

    // Clean up old history records to maintain retention limits
    scenarioMonitorHistoryRepo.deleteHistory(monitor.getId(), MAX_SCE_MONITOR_HISTORY_NUM);
    return history;
  }

  /**
   * Gets the repository for scenario monitoring history entities.
   * <p>
   * Used by the base command class for generic operations.
   * <p>
   * Provides access to the underlying scenario monitoring history data store.
   */
  @Override
  protected BaseRepository<ScenarioMonitorHistory, Long> getRepository() {
    return scenarioMonitorHistoryRepo;
  }

}
