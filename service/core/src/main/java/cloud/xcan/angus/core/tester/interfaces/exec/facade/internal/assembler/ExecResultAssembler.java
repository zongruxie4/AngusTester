package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.result.TestResultStatus;
import cloud.xcan.angus.api.commonlink.exec.result.TestResultSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestCaseResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestResultVo;
import cloud.xcan.angus.model.script.TestType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecResultAssembler {

  public static ExecTestResultDetailVo toExecResultVo(ExecTestResult testResultDb,
      ScriptInfo scriptInfo) {
    return new ExecTestResultDetailVo()
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
            toExecCaseResultVos(testResultDb.getCaseResults(), testResultDb.getExecName()))
        .setSampleContent(testResultDb.getSampleContent())
        .setExecBy(testResultDb.getExecBy())
        .setExecByName(testResultDb.getExecByName())
        .setLastExecDate(testResultDb.getLastExecDate())
        .setCreatedDate(testResultDb.getCreatedDate());
  }

  public static List<ExecTestCaseResultDetailVo> toExecCaseResultVos(
      List<ExecTestCaseResult> testResultDbs, String execName) {
    return isEmpty(testResultDbs) ? null : testResultDbs.stream()
        .map(x -> toExecCaseResultVo(x, execName)).collect(Collectors.toList());
  }

  public static ExecTestCaseResultDetailVo toExecCaseResultVo(ExecTestCaseResult testResultDb,
      String execName) {
    return new ExecTestCaseResultDetailVo()
        .setId(testResultDb.getId())
        .setExecId(testResultDb.getExecId())
        .setExecName(nullSafe(execName, testResultDb.getExecName()))
        .setExecStatus(testResultDb.getExecStatus())
        .setPlugin(testResultDb.getPlugin())
        .setScriptId(testResultDb.getScriptId())
        .setApisId(testResultDb.getApisId())
        .setCaseId(testResultDb.getCaseId())
        .setCaseName(testResultDb.getCaseName())
        .setCaseType(testResultDb.getCaseType())
        .setEnabled(testResultDb.getEnabled())
        .setPassed(testResultDb.getPassed())
        .setFailureMessage(testResultDb.getFailureMessage())
        .setTestNum(testResultDb.getTestNum())
        .setTestFailureNum(testResultDb.getTestFailureNum())
        .setAssertionSummary(testResultDb.getAssertionSummary())
        .setSampleContent(testResultDb.getSampleContent())
        .setExecBy(testResultDb.getExecBy())
        .setExecByName(testResultDb.getExecByName())
        .setLastExecDate(testResultDb.getLastExecDate())
        .setCreatedDate(testResultDb.getCreatedDate());
  }

  public static void assembleExecTestResultVo0(List<TestType> enabledTestTypes,
      List<ExecTestResult> result, Map<Long, ScriptInfo> scriptInfosMap,
      ExecTestResultVo resultVo) {

    resultVo.setPassed(enabledTestTypes.isEmpty() /*No enabled test*/
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
    resultVo.setResultSummary(resultSummary);

    resultVo.setEnabledTestTypes(enabledTestTypes.stream().map(TestType::toScriptType)
        .collect(Collectors.toList()));

    resultVo.setResultDetailVoMap(result.stream().collect(
        Collectors.toMap(ExecTestResult::getScriptType, x -> toExecResultVo(x,
            scriptInfosMap.get(x.getScriptId())))));
  }
}
