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

  @Override
  public ExecDebug findByScriptId(Long id) {
    return execDebugRepo.findBySourceAndScriptId(ExecDebugSource.SCRIPT, id);
  }

  @Override
  public ExecDebug findByScenarioId(Long scenarioId) {
    return execDebugRepo.findBySourceAndScenarioId(ExecDebugSource.SCENARIO, scenarioId);
  }

  @Override
  public ExecDebug findByMonitorId(Long monitorId) {
    return execDebugRepo.findBySourceAndMonitorId(ExecDebugSource.MONITOR, monitorId);
  }

  @Override
  public ExecDebug checkAndFind(Long id) {
    return execDebugRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ExecDebug"));
  }

  @Override
  public void checkAddQuota(long incr) {
    if (incr > 0) {
      long num = execDebugRepo.countByTenantId(getOptTenantId());
      settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterExecutionDebug,
          null, num + incr);
    }
  }

  @SneakyThrow0(level = "ERROR")
  @Override
  public void checkConcurrentTaskQuota(long incr) {
    if (incr > 0) {
      long num = execDebugRepo.countByTenantIdAndStatus(getOptTenantId(), ExecStatus.RUNNING);
      settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterConcurrentTask,
          null, num + incr);
    }
  }

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

  private void setParsedScriptContent(ExecDebug debug) {
    try {
      AngusScript angusScript = AngusParser.YAML_MAPPER
          .readValue(debug.getScript(), AngusScript.class);
      debug.setConfiguration(angusScript.getConfiguration());
      debug.setTask(angusScript.getTask());
      if (isNotEmpty(angusScript.getTask().getPipelines())) {
        PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
        // Fix: Single task pipeline name can be empty// Fix: Single task pipeline name can be empty// Fix: Single task pipeline name can be empty
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
