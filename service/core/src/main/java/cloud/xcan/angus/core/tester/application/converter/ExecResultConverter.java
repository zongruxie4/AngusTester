package cloud.xcan.angus.core.tester.application.converter;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.angus.api.commonlink.exec.TestResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecSampleResultContent;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import java.util.List;

public class ExecResultConverter {

  public static ExecSampleResultContent toTestResultContent(ExecSampleContent x) {
    return new ExecSampleResultContent()
        .setNodeId(x.getNodeId())
        .setTimestamp(x.getTimestamp()).setTimestamp0(x.getTimestamp0())
        .setName(x.getName()).setIteration(x.getIteration())
        .setKey(x.getKey()).setSampleResult(x.getSampleResult());
  }

  public static TestResultInfo assembleTestResultInfo(Exec execDb, ExecTestResult testResult) {
    return new TestResultInfo()
        .setScriptType(execDb.getScriptType())
        .setScriptId(execDb.getScriptId())
        .setScriptSource(execDb.getScriptSource())
        .setScriptSourceId(execDb.getScriptSourceId())
        .setPassed(testResult.isPassed())
        .setFailureMessage(testResult.getFailureMessage())
        .setTestNum(testResult.getTestNum())
        .setTestFailureNum(testResult.getTestFailureNum())
        .setExecId(execDb.getId())
        .setExecName(execDb.getName())
        .setLastExecStartDate(execDb.getActualStartDate())
        .setLastExecEndDate(execDb.getEndDate())
        .setExecBy(execDb.getExecBy());
  }


  public static List<TestCaseResultInfo> assembleCaseResultInfo(
      Exec execDb, List<ExecTestCaseResult> caseResults) {
    return isEmpty(caseResults) ? null : caseResults.stream().map(
            x -> new TestCaseResultInfo()
                .setCaseId(x.getCaseId())
                .setPassed(x.getPassed())
                .setFailureMessage(x.getFailureMessage())
                .setTestNum(x.getTestNum())
                .setTestFailureNum(x.getTestFailureNum())
                .setExecId(execDb.getId())
                .setExecName(execDb.getName())
                .setLastExecDate(execDb.getEndDate())
                .setExecBy(x.getExecBy()))
        .toList();
  }

}
