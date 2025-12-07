package cloud.xcan.angus.core.tester.application.query.exec.impl;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.model.AngusConstant.SAMPLE_TOTAL_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.SneakyThrow0;
import cloud.xcan.angus.core.tester.application.query.exec.ExecDebugQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleExtcQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleQuery;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugRepo;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugSource;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.model.SampleResult;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.meter.MeterStatus;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Implementation of ExecDebugQuery interface for managing debug execution queries.
 * <p>
 * This class provides functionality to query and retrieve debug execution information for scripts,
 * scenarios, and monitors. It handles quota checking, script parsing, and result evaluation for
 * debug executions.
 * <p>
 * The implementation includes methods for finding debug executions by different sources, checking
 * quotas, and setting debug results with parsed script content and sample data.
 */
@Biz
public class ExecDebugQueryImpl implements ExecDebugQuery {

  @Resource
  private ExecDebugRepo execDebugRepo;

  @Resource
  private ExecSampleExtcQuery execSampleExtcQuery;

  @Resource
  private ExecSampleQuery execSampleQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  /**
   * Retrieves detailed debug information for a script execution.
   * <p>
   * Finds the debug execution by script ID and sets the complete debug result including parsed
   * script content, sample contents, and execution success status.
   * <p>
   * Uses SneakyThrow0 annotation to handle quota checking errors gracefully.
   *
   * @param scriptId the ID of the script to find debug information for
   * @return ExecDebug object with complete debug information, or null if not found
   */
  @SneakyThrow0(level = "ERROR") // Check debug quota in running
  @Override
  public ExecDebug scriptDetail(Long scriptId) {
    return new BizTemplate<ExecDebug>() {

      @Override
      protected ExecDebug process() {
        ExecDebug debugDb = findByScriptId(scriptId);
        if (isNull(debugDb)) {
          return null;
        }
        setDebugResult(debugDb);
        return debugDb;
      }
    }.execute();
  }

  /**
   * Retrieves detailed debug information for a scenario execution.
   * <p>
   * Finds the debug execution by scenario ID and sets the complete debug result including parsed
   * script content, sample contents, and execution success status.
   * <p>
   * Uses SneakyThrow0 annotation to handle quota checking errors gracefully.
   *
   * @param scenarioId the ID of the scenario to find debug information for
   * @return ExecDebug object with complete debug information, or null if not found
   */
  @SneakyThrow0(level = "ERROR") // Check debug quota in running
  @Override
  public ExecDebug scenarioDetail(Long scenarioId) {
    return new BizTemplate<ExecDebug>() {

      @Override
      protected ExecDebug process() {
        ExecDebug debugDb = findByScenarioId(scenarioId);
        if (isNull(debugDb)) {
          return null;
        }
        setDebugResult(debugDb);
        return debugDb;
      }
    }.execute();
  }

  /**
   * Retrieves detailed debug information for a monitor execution.
   * <p>
   * Finds the debug execution by monitor ID and sets the complete debug result including parsed
   * script content, sample contents, and execution success status.
   * <p>
   * Uses SneakyThrow0 annotation to handle quota checking errors gracefully.
   *
   * @param monitorId the ID of the monitor to find debug information for
   * @return ExecDebug object with complete debug information, or null if not found
   */
  @SneakyThrow0(level = "ERROR") // Check debug quota in running
  @Override
  public ExecDebug monitorDetail(Long monitorId) {
    return new BizTemplate<ExecDebug>() {

      @Override
      protected ExecDebug process() {
        ExecDebug debugDb = findByMonitorId(monitorId);
        if (isNull(debugDb)) {
          return null;
        }
        setDebugResult(debugDb);
        return debugDb;
      }
    }.execute();
  }

  /**
   * Finds a debug execution by script ID.
   *
   * @param id the script ID to search for
   * @return ExecDebug object if found, null otherwise
   */
  @Override
  public ExecDebug findByScriptId(Long id) {
    return execDebugRepo.findBySourceAndScriptId(ExecDebugSource.SCRIPT, id);
  }

  /**
   * Finds a debug execution by scenario ID.
   *
   * @param scenarioId the scenario ID to search for
   * @return ExecDebug object if found, null otherwise
   */
  @Override
  public ExecDebug findByScenarioId(Long scenarioId) {
    return execDebugRepo.findBySourceAndScenarioId(ExecDebugSource.SCENARIO, scenarioId);
  }

  /**
   * Finds a debug execution by monitor ID.
   *
   * @param monitorId the monitor ID to search for
   * @return ExecDebug object if found, null otherwise
   */
  @Override
  public ExecDebug findByMonitorId(Long monitorId) {
    return execDebugRepo.findBySourceAndMonitorId(ExecDebugSource.MONITOR, monitorId);
  }

  /**
   * Finds a debug execution by ID and throws ResourceNotFound if not found.
   *
   * @param id the debug execution ID to search for
   * @return ExecDebug object if found
   * @throws ResourceNotFound if the debug execution is not found
   */
  @Override
  public ExecDebug checkAndFind(Long id) {
    return execDebugRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ExecDebug"));
  }

  /**
   * Checks if adding the specified increment would exceed the tenant's debug quota.
   * <p>
   * Only performs the check if the increment is greater than 0.
   * <p>
   * Queries the current count of debug executions for the tenant and validates against the
   * configured quota limits.
   *
   * @param incr the increment to check against the quota
   */
  @Override
  public void checkAddQuota(long incr) {
    if (incr > 0) {
      long num = execDebugRepo.countByTenantId(getOptTenantId());
      settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterExecutionDebug,
          null, num + incr);
    }
  }

  /**
   * Checks if adding the specified increment would exceed the tenant's concurrent task quota.
   * <p>
   * Only performs the check if the increment is greater than 0.
   * <p>
   * Queries the current count of running debug executions for the tenant and validates against the
   * configured concurrent task quota limits.
   *
   * @param incr the increment to check against the concurrent task quota
   */
  @SneakyThrow0(level = "ERROR")
  @Override
  public void checkConcurrentTaskQuota(long incr) {
    if (incr > 0) {
      long num = execDebugRepo.countByTenantIdAndStatus(getOptTenantId(), ExecStatus.RUNNING);
      settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterConcurrentTask,
          null, num + incr);
    }
  }

  /**
   * Sets the complete debug result for an ExecDebug object.
   * <p>
   * This method populates the debug object with parsed script content, sample contents, finish
   * sample result, and determines the execution success status.
   * <p>
   * The method performs the following operations: - Parses and sets the script content
   * configuration and task - Retrieves and sets sample contents for the execution - Gets the final
   * sample result for the execution - Judges and sets the execution success status
   *
   * @param debugDb the ExecDebug object to populate with result data
   */
  @Override
  public void setDebugResult(ExecDebug debugDb) {
    setParsedScriptContent(debugDb);

    List<ExecSampleContent> sampleContents = execSampleExtcQuery
        .findIterationSampleContent(debugDb.getId());
    debugDb.setSampleContents(sampleContents);

    // NOOP:: Instead by SampleResult#getFailMessage()
    // List<ExecSampleErrorCause> errorCauses =
    //    execSampleErrorContentQuery.findErrorContent(debugDb.getId());
    // debugDb.setErrorCauses(errorCauses);

    ExecSample sampleResult = execSampleQuery.getSingleTaskExecSampleResult(debugDb.getId());
    debugDb.setFinishSampleResult(sampleResult);

    judgeExecSuccess(debugDb);
  }

  /**
   * Judges the execution success status based on multiple criteria.
   * <p>
   * Evaluates the execution success by checking: - Whether the execution status is completed -
   * Meter status success (if available) - Sample result success for all sample contents
   * <p>
   * Sets the succeed flag and failure message accordingly.
   *
   * @param debug the ExecDebug object to evaluate and update
   */
  private void judgeExecSuccess(ExecDebug debug) {
    boolean succeed = debug.getStatus().isCompleted();
    String failureMessage = succeed ? null : debug.getMessage();
    if (succeed) {
      if (isNotEmpty(debug.getMeterStatus()) && !MeterStatus.of(debug.getMeterStatus())
          .isSuccess()) {
        succeed = false;
        failureMessage = nullSafe(debug.getMeterMessage(), failureMessage);
      }
      if (succeed) {
        SampleResult<?> result = debug.getSampleContents().stream()
            .filter(x -> nonNull(x.getSampleResult()) && !x.getSampleResult().isSuccess())
            .map(ExecSampleContent::getSampleResult).findFirst().orElse(null);
        if (nonNull(result)) {
          succeed = false;
          failureMessage = nullSafe(result.getFailMessage(), failureMessage);
        }
      }
    }
    debug.setSucceed(succeed).setFailureMessage(failureMessage);
  }

  /**
   * Parses and sets the script content for an ExecDebug object.
   * <p>
   * Deserializes the script content from YAML format and sets the configuration, task, and pipeline
   * target mappings. Handles cases where pipeline names might be empty for single task executions.
   * <p>
   * If parsing fails, throws a SysException with the error details.
   *
   * @param debug the ExecDebug object to set parsed script content for
   * @throws SysException if script content format is invalid
   */
  private void setParsedScriptContent(ExecDebug debug) {
    try {
      AngusScript angusScript = AngusParser.YAML_MAPPER
          .readValue(debug.getScript(), AngusScript.class);
      debug.setConfiguration(angusScript.getConfiguration());
      debug.setTask(angusScript.getTask());
      if (isNotEmpty(angusScript.getTask().getPipelines())) {
        PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
        // Fix: Single task pipeline name can be empty
        if (isNull(builder) || isEmpty(builder.getEnabledTargetNameMapping())) {
          LinkedHashMap<String, List<String>> pipelines = new LinkedHashMap<>();
          pipelines.put(SAMPLE_TOTAL_NAME, new ArrayList<>());
          debug.setPipelineTargetMappings(pipelines);
          return;
        }
        debug.setPipelineTargetMappings(builder.getEnabledTargetNameMapping());
      }
    } catch (Exception e) {
      throw SysException.of("Script content format error: " + e.getMessage());
    }
  }
}
