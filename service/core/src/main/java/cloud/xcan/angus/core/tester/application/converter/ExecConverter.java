package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.EXEC_MAX_FREE_DURATION_MS;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.EXEC_MAX_FREE_ITERATIONS;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.EXEC_MAX_FREE_NODES;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.EXEC_MAX_FREE_THREADS;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.model.AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS;
import static cloud.xcan.angus.model.AngusConstant.DEFAULT_REPORT_INTERVAL;
import static cloud.xcan.angus.model.script.AngusScript.safeReportInterval;
import static cloud.xcan.angus.parser.AngusParser.YAML_MAPPER;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.model.AngusConstant;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.NodeSelector;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.spec.unit.TimeValue;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.SneakyThrows;

public class ExecConverter {

  public static Exec localScriptVoToExec(Long projectId, String name, Long scriptId,
      String scriptContent, AngusScript angusScript, Boolean trial) {
    Exec exec = new Exec().setProjectId(projectId)
        .setName(stringSafe(name, "unnamed." + System.currentTimeMillis()))
        .setPlugin(angusScript.getPlugin())
        .setScriptType(angusScript.getType())
        .setScriptId(scriptId)
        .setScript(scriptContent)
        .setScriptCreatedBy(getUserId())
        .setScriptSource(null)
        .setScriptSourceId(null)
        .setStatus(ExecStatus.CREATED)
        .setSyncTestResult(false)
        .setSyncTestResultFailure(null)
        .setIterations(getSafeIterations(angusScript))
        .setDuration(angusScript.getConfiguration().getDuration())
        .setThread(angusScript.getConfiguration().getThread().getThreads())
        .setPriority(angusScript.getConfiguration().getPriority())
        .setIgnoreAssertions(judgeIgnoreAssertions(angusScript.getTask()))
        // Note: Script that are not generated based on scenarios and apis
        .setUpdateTestResult(/*judgeUpdateTestResult(angusScript.getTask())*/ false)
        .setStartMode(angusScript.getConfiguration().getStartMode())
        .setStartupTimeout(angusScript.getConfiguration().getFinalStartupTimeout())
        .setStartAtDate(angusScript.getConfiguration().getStartAtDate())
        // Force to 30 seconds after more than two hours
        .setReportInterval(safeReportInterval(angusScript.getConfiguration()))
        .setAssocApiCaseIds(angusScript.getAssocHttpCaseIds())
        .setTrial(nullSafe(trial, false))
        .setExecBy(getUserId())
        .setAvailableNodeIds(nonNull(angusScript.getConfiguration().getNodeSelectors())
            ? angusScript.getConfiguration().getNodeSelectors().getAvailableNodeIds() : null)
        .setAppNodeIds(nonNull(angusScript.getConfiguration().getNodeSelectors())
            ? angusScript.getConfiguration().getNodeSelectors().getAppNodeIds() : null)
        .setConfiguration(angusScript.getConfiguration())
        .setTask(angusScript.getTask())
        .setAngusScript(angusScript);
    PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
    if (nonNull(builder)) {
      LinkedHashMap<String, List<String>> pipelines = builder.getEnabledTargetNameMapping();
      exec.setSingleTargetPipeline(isEmpty(pipelines) /* Single task pipeline name can be empty*/
          || pipelines.size() == 1);
    }
    return exec;
  }

  @SneakyThrows
  public static Exec remoteScriptVoToExec(String name, ScriptType scriptType,
      Script script, AngusScript angusScript, Boolean trial) {
    Exec exec = new Exec().setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(stringSafe(name, script.getName()))
        .setPlugin(script.getPlugin())
        .setScriptType(nullSafe(scriptType, script.getType()))
        .setScriptId(script.getId())
        .setScript(YAML_MAPPER.writeValueAsString(angusScript))
        .setScriptCreatedBy(script.getCreatedBy())
        .setScriptSource(script.getSource())
        .setScriptSourceId(script.getSourceId())
        .setStatus(ExecStatus.CREATED)
        .setSyncTestResult(false)
        .setSyncTestResultFailure(null)
        .setIterations(getSafeIterations(angusScript))
        .setDuration(angusScript.getConfiguration().getDuration())
        .setThread(angusScript.getConfiguration().getThread().getThreads())
        .setPriority(angusScript.getConfiguration().getPriority())
        .setIgnoreAssertions(judgeIgnoreAssertions(angusScript.getTask()))
        .setUpdateTestResult(judgeUpdateTestResult0(script, angusScript.getTask()))
        .setStartMode(angusScript.getConfiguration().getStartMode())
        .setStartupTimeout(angusScript.getConfiguration().getFinalStartupTimeout())
        .setStartAtDate(angusScript.getConfiguration().getStartAtDate())
        // Force to 30 seconds after more than two hours
        .setReportInterval(safeReportInterval(angusScript.getConfiguration()))
        .setAssocApiCaseIds(angusScript.getAssocHttpCaseIds())
        .setTrial(nullSafe(trial, false))
        .setExecBy(getUserId())
        .setAvailableNodeIds(nonNull(angusScript.getConfiguration().getNodeSelectors())
            ? angusScript.getConfiguration().getNodeSelectors().getAvailableNodeIds() : null)
        .setAppNodeIds(nonNull(angusScript.getConfiguration().getNodeSelectors())
            ? angusScript.getConfiguration().getNodeSelectors().getAppNodeIds() : null)
        .setConfiguration(angusScript.getConfiguration())
        .setTask(angusScript.getTask())
        .setAngusScript(angusScript);
    PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
    if (nonNull(builder)) {
      LinkedHashMap<String, List<String>> pipelines = builder.getEnabledTargetNameMapping();
      exec.setSingleTargetPipeline(isEmpty(pipelines) /* Single task pipeline name can be empty*/
          || pipelines.size() == 1);
    }
    if (!isUserAction()){
      exec.setTenantId(script.getTenantId())
          .setCreatedBy(-1L).setCreatedDate(LocalDateTime.now())
          .setLastModifiedBy(-1L).setLastModifiedDate(LocalDateTime.now());
    }
    return exec;
  }

  public static Exec configReplaceToExec(Exec execDb, String name, ScriptType scriptType,
      Configuration configuration, AngusScript script) {
    return execDb.setName(emptySafe(name, execDb.getName()))
        .setScriptType(nullSafe(scriptType, execDb.getScriptType()))
        .setIterations(getSafeIterations(script))
        .setDuration(configuration.getDuration())
        .setThread(configuration.getThread().getThreads())
        .setPriority(configuration.getPriority())
        .setIgnoreAssertions(judgeIgnoreAssertions(script.getTask()))
        .setUpdateTestResult(judgeUpdateTestResult0(execDb, script.getTask()))
        .setStartMode(configuration.getStartMode())
        .setStartAtDate(configuration.getStartAtDate())
        .setStartupTimeout(configuration.getFinalStartupTimeout())
        .setReportInterval(nullSafe(configuration.getReportInterval(), DEFAULT_REPORT_INTERVAL))
        .setAvailableNodeIds(nonNull(configuration.getNodeSelectors())
            ? configuration.getNodeSelectors().getAvailableNodeIds() : null)
        .setAppNodeIds(nonNull(configuration.getNodeSelectors())
            ? configuration.getNodeSelectors().getAppNodeIds() : null);
  }

  public static void replaceOptionalConfig(Exec execDb, String name, Long iterations,
      TimeValue duration, Integer thread, Integer priority, Boolean ignoreAssertions,
      StartMode startMode, LocalDateTime startAtDate, TimeValue reportInterval) {
    if (isNotEmpty(name)) {
      execDb.setName(name);
    }
    if (nonNull(iterations)) {
      execDb.setIterations(iterations);
    }
    if (nonNull(duration)) {
      execDb.setDuration(duration);
    }
    if (nonNull(thread)) {
      execDb.setThread(thread);
    }
    if (nonNull(priority)) {
      execDb.setPriority(priority);
    }
    if (nonNull(ignoreAssertions)) {
      execDb.setIgnoreAssertions(ignoreAssertions);
    }
    if (nonNull(startMode)) {
      execDb.setStartMode(startMode);
    }
    if (nonNull(startAtDate)) {
      execDb.setStartAtDate(startAtDate);
    }
    if (nonNull(reportInterval)) {
      // Force to 30 seconds after more than two hours
      execDb.setReportInterval(safeReportInterval(Configuration.newBuilder()
          .reportInterval(reportInterval).duration(duration)
          .build()));
    }
  }

  @SneakyThrows
  public static String overwriteConfigScript(Exec exec, Integer thread,
      Integer shardingRampUpThread, Integer shardingRampDownThread, Long iterations) {
    AngusScript angusScript = YAML_MAPPER.readValue(exec.getScript(), AngusScript.class);
    return assembleOverwriteConfigScript0(exec, thread, shardingRampUpThread,
        shardingRampDownThread, iterations, angusScript);
  }

  public static String overwriteConfigScript(Exec exec, AngusScript angusScript,
      Integer thread, Integer shardingRampUpThread, Integer shardingRampDownThread,
      Long iterations) {
    return assembleOverwriteConfigScript0(exec, thread, shardingRampUpThread,
        shardingRampDownThread, iterations, angusScript);
  }

  @SneakyThrows
  private static String assembleOverwriteConfigScript0(Exec exec, Integer thread,
      Integer shardingRampUpThread, Integer shardingRampDownThread,
      Long iterations, AngusScript angusScript) {
    angusScript.getConfiguration()
        .setIterations(nonNull(iterations) ? iterations : exec.getIterations());
    angusScript.getConfiguration().setDuration(exec.getDuration());
    angusScript.getConfiguration().getThread()
        .setThreads(nonNull(thread) ? thread : exec.getThread());
    if (nonNull(shardingRampUpThread)
        && angusScript.getConfiguration().getThread().needRampUp()) {
      angusScript.getConfiguration().getThread().setRampUpThreads(shardingRampUpThread);
    }
    if (nonNull(shardingRampDownThread)
        && angusScript.getConfiguration().getThread().needRampDown()) {
      angusScript.getConfiguration().getThread().setRampDownThreads(shardingRampDownThread);
    }
    // private int priority;
    // private StartMode startMode;
    // private LocalDateTime startAtDate;
    angusScript.getConfiguration().setReportInterval(exec.getReportInterval());
    if (nonNull(exec.getIgnoreAssertions())) {
      angusScript.getTask().addArgument(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS,
          exec.getIgnoreAssertions());
    }
    if (nonNull(exec.getUpdateTestResult())) {
      angusScript.getTask().addArgument(AngusConstant.ARGUMENT_KEY_NAME_UPDATE_TEST_RESULT,
          exec.getUpdateTestResult());
    }
    if (nonNull(exec.getIterations()) && nonNull(angusScript.getTask().getMockData())) {
      angusScript.getTask().getMockData().getSettings().setRows(exec.getIterations());
    }
    exec.setConfiguration(angusScript.getConfiguration());
    exec.setTask(angusScript.getTask());
    exec.setAngusScript(angusScript);
    return YAML_MAPPER.writeValueAsString(angusScript);
  }

  public static void safeTrialConfiguration(Configuration configuration) {
    if (isNull(configuration.getNodeSelectors())
        || isNull(configuration.getNodeSelectors().getNum())) {
      configuration.setNodeSelectors(NodeSelector.newBuilder().num(1).build());
    } else if (configuration.getNodeSelectors().getNum() > EXEC_MAX_FREE_NODES) {
      configuration.getNodeSelectors().setNum(EXEC_MAX_FREE_NODES);
    }
    // Note: Free experience execution does not support node selection strategy.
    configuration.getNodeSelectors().setStrategy(null);
    if (nonNull(configuration.getThread())
        && configuration.getThread().getThreads() > EXEC_MAX_FREE_THREADS) {
      configuration.getThread().setThreads(EXEC_MAX_FREE_THREADS);
    }
    if (nonNull(configuration.getDuration())
        && configuration.getDuration().toMilliSecond() > EXEC_MAX_FREE_DURATION_MS) {
      configuration.setDuration(TimeValue.ofMillisecond(EXEC_MAX_FREE_DURATION_MS));
    }
    if (nonNull(configuration.getIterations())
        && configuration.getIterations() > EXEC_MAX_FREE_ITERATIONS) {
      configuration.setIterations(EXEC_MAX_FREE_ITERATIONS);
    }
  }

  public static Long getSafeIterations(AngusScript script) {
    if (script.getType().isMockData() && isNull(script.getConfiguration().getIterations())) {
      return script.getTask().getMockData().getSettings().getRows();
    }
    return script.getConfiguration().getIterations();
  }

  public static Boolean judgeIgnoreAssertions(Task task) {
    if (isNull(task.getArguments())
        || !task.getArguments().containsKey(ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS)) {
      return true; // By default
    }
    Object value = task.getArguments().get(ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS);

    // No need to update test results
    return Boolean.parseBoolean(value.toString());
  }

  public static boolean judgeUpdateTestResult0(Script script, Task task) {
    if (isNull(script.getSourceId()) || isNull(script.getSource())
        || !script.getSource().needTestResult()) {
      return false;
    }
    //return judgeUpdateTestResultByPipelineSource(scriptInfoVo.getPlugin(), task);
    return true;
  }

  public static void judgeUpdateTestResult(Exec execDb, Boolean updateTestResult) {
    if (nonNull(updateTestResult)) {
      boolean needUpdateTestResult = ExecConverter.judgeUpdateTestResult0(execDb,
          execDb.getAngusScript().getTask());
      if (needUpdateTestResult) {
        execDb.setUpdateTestResult(updateTestResult);
        // Update the test results again
        if (updateTestResult && execDb.isSyncTestResult()){
          execDb.setSyncTestResult(false);
        }
      } else {
        execDb.setUpdateTestResult(null);
      }
    }
  }

  public static boolean judgeUpdateTestResult0(Exec execDb, Task task) {
    if (isNull(execDb.getScriptSourceId()) || isNull(execDb.getScriptSource())
        || !execDb.getScriptSource().needTestResult()) {
      return false;
    }
    //return judgeUpdateTestResultByPipelineSource(execDb.getPlugin(), task);
    return true;
  }

  /*public static boolean judgeUpdateTestResultByPipelineSource(String plugin, Task task) {
    AngusScript angusScript = new AngusScript();
    angusScript.setPlugin(plugin);
    angusScript.setTask(task);

    // need to update test results
    return angusScript.needsUpdateTestResult();
  }*/
}
