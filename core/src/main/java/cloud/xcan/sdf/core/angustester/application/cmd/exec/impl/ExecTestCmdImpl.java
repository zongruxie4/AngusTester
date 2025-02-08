package cloud.xcan.sdf.core.angustester.application.cmd.exec.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.sdf.api.commonlink.exec.TestResultInfo;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCase;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.model.script.TestType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ExecTestCmdImpl implements ExecTestCmd {

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ApisCaseRepo apisCaseRepo;

  /**
   * Note: A scenario or apis may have multiple testing tasks.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testResultUpdate(TestResultInfo testResult, List<TestCaseResultInfo> caseResults) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (nonNull(testResult)) {
          updateTaskTestResult(testResult);
        }

        if (testResult.getScriptSource().isApis()) {
          updateApisTestResult(testResult);
        } else if (testResult.getScriptSource().isScenario()) {
          updateScenarioTestResult(testResult);
        }

        if (isNotEmpty(caseResults)) {
          setApisCaseTestResult(caseResults);
        }
        return null;
      }
    }.execute();
  }

  private void updateTaskTestResult(TestResultInfo testResult) {
    List<Task> taskDbs = taskRepo.find0ByTargetIdAndTestType(testResult.getScriptSourceId(),
        TestType.of(testResult.getScriptType()).getValue());
    if (isNotEmpty(taskDbs)) {
      for (Task taskDb : taskDbs) {
        setTaskTestResult(taskDb, testResult);
      }
      taskRepo.saveAll(taskDbs);
    }
  }

  private void updateApisTestResult(TestResultInfo testResult) {
    Apis apisDb = apisRepo.findById(testResult.getScriptSourceId()).orElse(null);
    if (nonNull(apisDb)) {
      switch (testResult.getScriptType()) {
        case TEST_FUNCTIONALITY:
          apisDb.setTestFuncPassedFlag(testResult.isPassed())
              .setTestFuncFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_PERFORMANCE:
          apisDb.setTestPerfPassedFlag(testResult.isPassed())
              .setTestPerfFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_STABILITY:
          apisDb.setTestStabilityPassedFlag(testResult.isPassed())
              .setTestStabilityFailureMessage(testResult.getFailureMessage());
          break;
      }
      apisRepo.save(apisDb);
    }
  }

  private void updateScenarioTestResult(TestResultInfo testResult) {
    Scenario scenarioDb = scenarioRepo.findById(testResult.getScriptSourceId()).orElse(null);
    if (nonNull(scenarioDb)) {
      switch (testResult.getScriptType()) {
        case TEST_FUNCTIONALITY:
          scenarioDb.setTestFuncPassedFlag(testResult.isPassed())
              .setTestFuncFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_PERFORMANCE:
          scenarioDb.setTestPerfPassedFlag(testResult.isPassed())
              .setTestPerfFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_STABILITY:
          scenarioDb.setTestStabilityPassedFlag(testResult.isPassed())
              .setTestStabilityFailureMessage(testResult.getFailureMessage());
          break;
      }
      scenarioRepo.save(scenarioDb);
    }
  }

  private void setApisCaseTestResult(List<TestCaseResultInfo> caseResults) {
    Map<Long, TestCaseResultInfo> caseResultInfoMap = caseResults.stream()
        .filter(x -> nonNull(x.getPassed())) // A null value means not executed, disabled.
        .collect(Collectors.toMap(TestCaseResultInfo::getCaseId, x -> x));
    List<ApisCase> caseDbs = apisCaseRepo.findAllById(caseResultInfoMap.keySet());
    if (isNotEmpty(caseDbs)) {
      for (ApisCase caseDb : caseDbs) {
        setCaseTestResult(caseDb, caseResultInfoMap.get(caseDb.getId()));
      }
      apisCaseRepo.saveAll(caseDbs);
    }
  }

  private void setTaskTestResult(Task taskDb, TestResultInfo resultInfo) {
    taskDb.setStatus(resultInfo.isPassed() ? TaskStatus.COMPLETED : taskDb.getStatus())
        .setScriptId(resultInfo.getScriptId())
        .setExecResult(resultInfo.isPassed() ? Result.SUCCESS : Result.FAIL)
        .setExecFailureMessage(resultInfo.getFailureMessage())
        .setExecTestNum(resultInfo.getTestNum())
        .setExecTestFailureNum(resultInfo.getTestFailureNum())
        .setExecId(resultInfo.getExecId())
        .setExecName(resultInfo.getExecName())
        .setExecBy(resultInfo.getExecBy())
        .setExecDate(resultInfo.getLastExecStartDate());

    if (taskDb.getStatus().isCompleted()) {
      taskDb.setStartDate(resultInfo.getLastExecStartDate());
      taskDb.setCompletedDate(resultInfo.getLastExecEndDate());
    }
    if (!resultInfo.isPassed()) {
      taskDb.setFailNum(nullSafe(taskDb.getFailNum(), 0) + 1);
    }
    taskDb.setTotalNum(nullSafe(taskDb.getFailNum(), 0) + 1);
  }

  private void setCaseTestResult(ApisCase apisCaseDb, TestCaseResultInfo caseResult) {
    apisCaseDb.setExecResult(caseResult.getPassed() ? Result.SUCCESS : Result.FAIL)
        .setExecFailureMessage(caseResult.getFailureMessage())
        .setExecTestNum(caseResult.getTestNum())
        .setExecTestFailureNum(caseResult.getTestFailureNum())
        .setExecId(caseResult.getExecId())
        .setExecName(caseResult.getExecName())
        .setExecBy(caseResult.getExecBy())
        .setExecDate(caseResult.getLastExecDate());
  }

}
