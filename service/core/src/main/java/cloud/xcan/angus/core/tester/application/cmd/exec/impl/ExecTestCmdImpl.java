package cloud.xcan.angus.core.tester.application.cmd.exec.impl;

import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.angus.api.commonlink.exec.TestResultInfo;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoRepo;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing test result updates and example imports for executions.
 * </p>
 * <p>
 * Provides methods for updating test results and importing example executions. Handles result
 * aggregation, status updates, and related entity updates for tasks, APIs, scenarios, and cases
 * based on execution outcomes.
 * </p>
 * <p>
 * Key features include comprehensive test result synchronization across multiple entity types and
 * example execution import functionality.
 * </p>
 */
@Service
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
  private ExecCmd execCmd;

  /**
   * <p>
   * Update test results for execution.
   * </p>
   * <p>
   * Note: A scenario or APIs may have multiple testing tasks. Updates test results for tasks, APIs,
   * scenarios, and cases based on execution outcome. Handles result aggregation and status
   * synchronization across related entities.
   * </p>
   *
   * @param testResult  Test result information
   * @param caseResults List of test case results
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testResultUpdate(TestResultInfo testResult, List<TestCaseResultInfo> caseResults) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        // Update API or scenario test results based on script source
        if (testResult.getScriptSource().isApis()) {
          updateApisTestResult(testResult);
        } else if (testResult.getScriptSource().isScenario()) {
          updateScenarioTestResult(testResult);
        }

        // Update API case test results if case results are provided
        if (isNotEmpty(caseResults)) {
          setApisCaseTestResult(caseResults);
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Import example execution for project.
   * </p>
   * <p>
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext. Finds performance test script and creates example execution.
   * </p>
   *
   * @param projectId Project ID
   * @return Execution ID and name, or null if no suitable script found
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> importExample(Long projectId) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected IdKey<Long, Object> process() {
        // Find top performance test script for the project
        ScriptInfo script = scriptInfoRepo.findTop1ByProjectIdAndPluginAndTypeIn(projectId,
            PLUGIN_HTTP_NAME, ScriptType.TEST_PERFORMANCE.getValue());

        if (isNull(script)) {
          return null;
        }

        // Create example execution using the found script
        return execCmd.addByRemoteScript(
            script.getName() + "-" + script.getType().getMessage(), script.getId(),
            script.getType(), null, new Arguments(), applicationInfo.isCloudServiceEdition());
      }
    }.execute();
  }

  /**
   * <p>
   * Update test results for APIs.
   * </p>
   * <p>
   * Updates the test result for an API based on the execution outcome. Handles different test types
   * (functional, performance, stability) and updates corresponding fields in the API entity.
   * </p>
   *
   * @param testResult Test result information
   */
  private void updateApisTestResult(TestResultInfo testResult) {
    // Find API by script source ID
    Apis apisDb = apisRepo.findById(testResult.getScriptSourceId()).orElse(null);
    if (nonNull(apisDb)) {
      // Update API test results based on script type
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
        case TEST_CUSTOMIZATION:
        case MOCK_APIS:
        case MOCK_DATA:
          // No action required for MOCK_DATA
          break;
      }
      apisRepo.save(apisDb);
    }
  }

  /**
   * <p>
   * Update test results for scenarios.
   * </p>
   * <p>
   * Updates the test result for a scenario based on the execution outcome. Handles different test
   * types (functional, performance, stability) and updates corresponding fields in the scenario
   * entity.
   * </p>
   *
   * @param testResult Test result information
   */
  private void updateScenarioTestResult(TestResultInfo testResult) {
    // Find scenario by script source ID
    Scenario scenarioDb = scenarioRepo.findById(testResult.getScriptSourceId()).orElse(null);
    if (nonNull(scenarioDb)) {
      // Update scenario test results based on script type
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
        case TEST_CUSTOMIZATION:
        case MOCK_APIS:
        case MOCK_DATA:
          // No action required for MOCK_DATA
          break;
      }
      scenarioRepo.save(scenarioDb);
    }
  }

  /**
   * <p>
   * Set test results for API cases.
   * </p>
   * <p>
   * Updates the test result for each API case based on the execution outcome. Filters out cases
   * with null passed status (not executed, disabled) and updates execution details for enabled
   * cases.
   * </p>
   *
   * @param caseResults List of test case results
   */
  private void setApisCaseTestResult(List<TestCaseResultInfo> caseResults) {
    // Filter and map case results by case ID (exclude null passed status)
    Map<Long, TestCaseResultInfo> caseResultInfoMap = caseResults.stream()
        .filter(x -> nonNull(x.getPassed())) // A null value means not executed, disabled.
        .collect(Collectors.toMap(TestCaseResultInfo::getCaseId, x -> x));

    // Find and update API cases
    List<ApisCase> caseDbs = apisCaseRepo.findAllById(caseResultInfoMap.keySet());
    if (isNotEmpty(caseDbs)) {
      for (ApisCase caseDb : caseDbs) {
        setCaseTestResult(caseDb, caseResultInfoMap.get(caseDb.getId()));
      }
      apisCaseRepo.saveAll(caseDbs);
    }
  }

  /**
   * <p>
   * Set test result for an API case.
   * </p>
   * <p>
   * Updates the execution result, failure message, test numbers, and execution details of an API
   * case based on the test result.
   * </p>
   *
   * @param apisCaseDb API case entity to update
   * @param caseResult Test case result information
   */
  private void setCaseTestResult(ApisCase apisCaseDb, TestCaseResultInfo caseResult) {
    // Update API case execution details and test numbers
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
