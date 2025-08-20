package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.result.TestResultStatus;
import cloud.xcan.angus.api.commonlink.exec.result.TestResultSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.model.script.TestType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecResultSummaryConverter {

  public static ExecTestResultDetailSummary toTestResultDetailSummary(
      ExecTestResult testResultDb, ScriptInfo scriptInfo) {
    return new ExecTestResultDetailSummary()
        .setId(testResultDb.getId())
        .setExecId(testResultDb.getExecId())
        .setExecName(testResultDb.getExecName())
        .setExecStatus(testResultDb.getExecStatus())
        .setPlugin(testResultDb.getPlugin())
        .setScriptType(testResultDb.getScriptType())
        .setScriptId(testResultDb.getScriptId())
        .setScriptName(nonNull(scriptInfo) ? scriptInfo.getName() : null)
        .setScriptSource(testResultDb.getScriptSource())
        .setScriptSourceId(testResultDb.getScriptSourceId())
        .setScriptSourceName(nonNull(scriptInfo) ? scriptInfo.getSourceName() : null)
        .setIndicatorFunc(testResultDb.getIndicatorFunc())
        .setIndicatorPerf(testResultDb.getIndicatorPerf())
        .setIndicatorStability(testResultDb.getIndicatorStability())
        .setPassed(testResultDb.isPassed())
        .setFailureMessage(testResultDb.getFailureMessage())
        .setTestNum(testResultDb.getTestNum())
        .setTestFailureNum(testResultDb.getTestFailureNum())
        .setUsageFailedNodeId(testResultDb.getUsageFailedNodeId())
        .setSampleSummary(testResultDb.getSampleSummary())
        .setTargetSummary(testResultDb.getTargetSummary())
        .setCaseSummary(testResultDb.getCaseSummary())
        .setNodeUsageSummary(testResultDb.getNodeUsageSummary())
        .setCaseResults(
            toTestCaseResultDetailSummaries(testResultDb.getCaseResults(), testResultDb.getExecName()))
        .setSampleContent(testResultDb.getSampleContent())
        .setExecBy(testResultDb.getExecBy())
        .setExecByName(testResultDb.getExecByName())
        .setLastExecDate(testResultDb.getLastExecDate())
        .setCreatedDate(testResultDb.getCreatedDate());
  }

  public static List<ExecTestCaseResultDetailSummary> toTestCaseResultDetailSummaries(
      List<ExecTestCaseResult> testResultDbs, String execName) {
    return isEmpty(testResultDbs) ? null : testResultDbs.stream()
        .map(x -> toTestCaseResultDetailSummary(x, execName)).toList();
  }

  public static ExecTestCaseResultDetailSummary toTestCaseResultDetailSummary(
      ExecTestCaseResult testCaseResult, String execName) {
    return new ExecTestCaseResultDetailSummary()
        .setId(testCaseResult.getId())
        .setExecId(testCaseResult.getExecId())
        .setExecName(nullSafe(execName, testCaseResult.getExecName()))
        .setExecStatus(testCaseResult.getExecStatus())
        .setPlugin(testCaseResult.getPlugin())
        .setScriptId(testCaseResult.getScriptId())
        .setApisId(testCaseResult.getApisId())
        .setCaseId(testCaseResult.getCaseId())
        .setCaseName(testCaseResult.getCaseName())
        .setCaseType(testCaseResult.getCaseType())
        .setEnabled(testCaseResult.getEnabled())
        .setPassed(testCaseResult.getPassed())
        .setFailureMessage(testCaseResult.getFailureMessage())
        .setTestNum(testCaseResult.getTestNum())
        .setTestFailureNum(testCaseResult.getTestFailureNum())
        .setAssertionSummary(testCaseResult.getAssertionSummary())
        .setSampleContent(testCaseResult.getSampleContent())
        .setExecBy(testCaseResult.getExecBy())
        .setExecByName(testCaseResult.getExecByName())
        .setLastExecDate(testCaseResult.getLastExecDate())
        .setCreatedDate(testCaseResult.getCreatedDate());
  }

  public static void assembleTestResultSummary(List<TestType> enabledTestTypes,
      List<ExecTestResult> result, Map<Long, ScriptInfo> scriptInfosMap,
      ExecTestResultSummary testResultSummary) {

    testResultSummary.setPassed(enabledTestTypes.isEmpty() /*No enabled test*/
        ? null : enabledTestTypes.size() == result.size()
        && result.stream().allMatch(ExecTestResult::isPassed));

    TestResultSummary resultSummary = new TestResultSummary();
    resultSummary.setTestNum(
        result.stream().map(ExecTestResult::getTestNum).reduce(Integer::sum).orElse(0));
    resultSummary.setTestFailureNum(
        result.stream().map(ExecTestResult::getTestFailureNum).reduce(Integer::sum).orElse(0));
    resultSummary.setTestSuccessNum(resultSummary.getTestNum() - resultSummary.getTestFailureNum());
    resultSummary.setTestSuccessRate(
        calcRate(resultSummary.getTestSuccessNum(), resultSummary.getTestNum()));
    TestResultStatus resultStatus;
    if (isEmpty(enabledTestTypes)) {
      resultStatus = TestResultStatus.NOT_ENABLED;
    } else if (isEmpty(result)) {
      resultStatus = TestResultStatus.UNTESTED;
    } else if (result.stream().allMatch(ExecTestResult::isPassed)) {
      resultStatus = TestResultStatus.FULLY_PASSED;
    } else if (result.stream().noneMatch(ExecTestResult::isPassed)) {
      resultStatus = TestResultStatus.FULLY_PASSED;
    } else {
      resultStatus = TestResultStatus.PARTIALLY_PASSED;
    }
    resultSummary.setResultStatus(resultStatus);
    testResultSummary.setResultSummary(resultSummary);

    testResultSummary.setEnabledTestTypes(enabledTestTypes.stream().map(TestType::toScriptType)
        .toList());

    testResultSummary.setResultDetailVoMap(result.stream().collect(
        Collectors.toMap(ExecTestResult::getScriptType, x -> toTestResultDetailSummary(x,
            scriptInfosMap.get(x.getScriptId())))));
  }
}
