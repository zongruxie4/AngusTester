package cloud.xcan.angus.core.tester.application.cmd.exec.impl;

import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.angus.api.commonlink.exec.TestResultInfo;
import cloud.xcan.angus.api.ctrl.exec.ExecDoorRemote;
import cloud.xcan.angus.api.ctrl.exec.dto.ExecAddByScriptDto;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseRepo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoRepo;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  @Resource
  private ScriptInfoRepo scriptInfoRepo;

  @Resource
  private ApplicationInfo applicationInfo;

  @Resource
  private ExecDoorRemote execDoorRemote;

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

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> importExample(Long projectId) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IdKey<Long, Object> process() {
        ScriptInfo script = scriptInfoRepo.findTop1ByProjectIdAndPluginAndTypeIn(projectId,
            PLUGIN_HTTP_NAME, ScriptType.TEST_PERFORMANCE.getValue());
        if (isNull(script)) {
          return null;
        }
        return execDoorRemote.addByScript(new ExecAddByScriptDto()
            .setName(script.getName() + "-" + script.getType().getMessage())
            .setScriptId(script.getId()).setScriptType(script.getType())
            .setTrial(applicationInfo.isCloudServiceEdition()))
            .orElseContentThrow();
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
          apisDb.setTestFuncPassed(testResult.isPassed())
              .setTestFuncFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_PERFORMANCE:
          apisDb.setTestPerfPassed(testResult.isPassed())
              .setTestPerfFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_STABILITY:
          apisDb.setTestStabilityPassed(testResult.isPassed())
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
          scenarioDb.setTestFuncPassed(testResult.isPassed())
              .setTestFuncFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_PERFORMANCE:
          scenarioDb.setTestPerfPassed(testResult.isPassed())
              .setTestPerfFailureMessage(testResult.getFailureMessage());
          break;
        case TEST_STABILITY:
          scenarioDb.setTestStabilityPassed(testResult.isPassed())
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
