package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.tester.application.converter.ExecResultSummaryConverter.assembleTestResultSummary;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecResultSummary;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecTestResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.TestResultCount;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.entity.projection.IdAndName;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecTestResultQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResultRepo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResultInfoRepo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResultRepo;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.model.apis.ApisInfo;
import cloud.xcan.angus.model.scenario.ScenarioInfo;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.services.ApisTestCount;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExecTestResultQuery for managing test result queries and analysis.
 * <p>
 * This class provides comprehensive functionality for querying and analyzing test results
 * from various sources including APIs, scenarios, and individual test cases. It handles
 * result aggregation, progress tracking, and detailed result analysis.
 * <p>
 * Supports different test types (functional, performance, stability) and provides
 * methods for assembling result summaries and progress information for reporting purposes.
 */
@Slf4j
@Biz
public class ExecTestResultQueryImpl implements ExecTestResultQuery {

  @Resource
  private ExecTestResultRepo execTestResultRepo;
  @Resource
  private ExecTestResultInfoRepo execTestResultInfoRepo;
  @Resource
  private ExecTestCaseResultRepo execTestCaseResultRepo;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ScenarioTestQuery scenarioTestQuery;
  @Resource
  private ExecQuery execQuery;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves test result for a specific execution.
   * <p>
   * Finds the test result by execution ID and populates it with execution name
   * and executor information. For API functional testing, also includes case results.
   *
   * @param execId the execution ID to get test result for
   * @return ExecTestResult object with complete test information
   */
  @Override
  public ExecTestResult execTestResult(Long execId) {
    return new BizTemplate<ExecTestResult>() {

      @Override
      protected ExecTestResult process() {
        ExecTestResult result = execTestResultRepo.findByExecId(execId);
        if (nonNull(result)) {
          setTestResultAndName(result);
        }
        return result;
      }
    }.execute();
  }

  /**
   * Retrieves test result by script source ID and script type.
   * <p>
   * Finds the test result for a specific source and script type combination.
   * This method is useful for getting results for specific test configurations.
   *
   * @param sourceId the script source ID
   * @param scriptType the script type to filter by
   * @return ExecTestResult object if found, null otherwise
   */
  @Override
  public ExecTestResult resultByScriptType(Long sourceId, ScriptType scriptType) {
    return new BizTemplate<ExecTestResult>() {

      @Override
      protected ExecTestResult process() {
        ExecTestResult result = execTestResultRepo.findByScriptSourceIdAndScriptType(sourceId,
            scriptType);
        if (nonNull(result)) {
          setTestResultAndName(result);
        }
        return result;
      }
    }.execute();
  }

  /**
   * Retrieves all test results for a specific source ID.
   * <p>
   * Finds all test results associated with the source ID and populates them with
   * execution names and executor information. For API functional testing,
   * includes case results for the first matching result.
   *
   * @param sourceId the script source ID to get results for
   * @return List of ExecTestResult objects
   */
  @Override
  public List<ExecTestResult> result(Long sourceId) {
    return new BizTemplate<List<ExecTestResult>>() {

      @Override
      protected List<ExecTestResult> process() {
        List<ExecTestResult> testResults = execTestResultRepo.findByScriptSourceId(sourceId);
        if (isNotEmpty(testResults)) {
          setResultExecName(testResults);
          setResultExecByName(testResults);

          for (ExecTestResult result : testResults) {
            if (result.getScriptType().isFunctionalTesting() && result.getScriptSource().isApis()) {
              result.setCaseResults(
                  execTestCaseResultRepo.findByApisId(result.getScriptSourceId()));
              break;
            }
          }
        }
        return testResults;
      }
    }.execute();
  }

  /**
   * Retrieves service APIs test result information.
   * <p>
   * Assembles comprehensive test result information for APIs within a service,
   * including test counts, progress tracking, and detailed result analysis.
   * <p>
   * Filters results based on creator object type and time range for targeted analysis.
   *
   * @param serviceId the service ID to analyze
   * @param creatorObjectType the creator object type for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return ExecApisResultInfo with comprehensive test analysis
   */
  @Override
  public ExecApisResultInfo serviceApisResult(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ExecApisResultInfo>() {

      @Override
      protected ExecApisResultInfo process() {
        ExecApisResultInfo result = new ExecApisResultInfo();

        ApisTestCount testApis = servicesQuery.countServiceTestApis(serviceId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isEmpty(testApis.getEnabledTestApiIds())) {
          return result.setProgress(new Progress()).setTestApis(testApis)
              .setTestResultCount(new TestResultCount());
        }
        assembleExecApisResultInfo(result, testApis);

        return result;
      }
    }.execute();
  }

  /**
   * Retrieves project APIs test result information.
   * <p>
   * Assembles comprehensive test result information for APIs within a project,
   * including test counts, progress tracking, and detailed result analysis.
   * <p>
   * Similar to serviceApisResult but operates at the project level for broader analysis.
   *
   * @param projectId the project ID to analyze
   * @param creatorObjectType the creator object type for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return ExecApisResultInfo with comprehensive test analysis
   */
  @Override
  public ExecApisResultInfo projectApisResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ExecApisResultInfo>() {

      @Override
      protected ExecApisResultInfo process() {
        ExecApisResultInfo result = new ExecApisResultInfo();

        ApisTestCount testApis = servicesQuery.countProjectTestApis(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isEmpty(testApis.getEnabledTestApiIds())) {
          return result.setProgress(new Progress()).setTestApis(testApis)
              .setTestResultCount(new TestResultCount());
        }
        assembleExecApisResultInfo(result, testApis);

        return result;
      }
    }.execute();
  }

  /**
   * Retrieves project scenario test result information.
   * <p>
   * Assembles comprehensive test result information for scenarios within a project,
   * including test counts, progress tracking, and detailed result analysis.
   * <p>
   * Provides scenario-level analysis similar to API analysis but for scenario-based testing.
   *
   * @param projectId the project ID to analyze
   * @param creatorObjectType the creator object type for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return ExecScenarioResultInfo with comprehensive test analysis
   */
  @Override
  public ExecScenarioResultInfo projectScenarioResult(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ExecScenarioResultInfo>() {

      @Override
      protected ExecScenarioResultInfo process() {
        ExecScenarioResultInfo result = new ExecScenarioResultInfo();

        ScenarioTestCount testScenarios = scenarioTestQuery.countProjectTestScenario(
            projectId, creatorObjectType, creatorObjectId, createdDateStart, createdDateEnd);
        if (isEmpty(testScenarios.getEnabledTestScenarioIds())) {
          return result.setProgress(new Progress()).setTestScenarios(testScenarios)
              .setTestResultCount(new TestResultCount());
        }
        assembleExecScenarioResultInfo(result, testScenarios);
        return result;
      }
    }.execute();
  }

  /**
   * Retrieves test case result for a specific case ID.
   * <p>
   * Finds the test case result and populates it with execution name and executor information.
   * This method provides detailed information about individual test case execution.
   *
   * @param caseId the case ID to get result for
   * @return ExecTestCaseResult object with complete case information
   */
  @Override
  public ExecTestCaseResult caseResult(Long caseId) {
    return new BizTemplate<ExecTestCaseResult>() {

      @Override
      protected ExecTestCaseResult process() {
        ExecTestCaseResult result = execTestCaseResultRepo.findByCaseId(caseId);
        if (nonNull(result)) {
          setCaseResultExecName(List.of(result));
          setCaseResultExecByName(List.of(result));
        }
        return result;
      }
    }.execute();
  }

  /**
   * Assembles execution test result summary for enabled test types.
   * <p>
   * Creates a comprehensive summary of test results for a script source ID,
   * filtering by enabled test types and including script information.
   * <p>
   * This method is used for generating high-level test result reports and dashboards.
   *
   * @param scriptSourceId the script source ID to summarize
   * @param enabledTestTypes the list of enabled test types to include
   * @return ExecTestResultSummary with comprehensive result summary
   */
  @Override
  public ExecTestResultSummary assembleExecTestResultSummary(Long scriptSourceId,
      List<TestType> enabledTestTypes) {
    ExecTestResultSummary resultVo = new ExecTestResultSummary();
    List<ExecTestResult> result = result(scriptSourceId);
    Map<Long, ScriptInfo> scriptInfosVoMap = scriptQuery.getScriptInfoMap(
        result.stream().map(ExecTestResult::getScriptId).collect(Collectors.toSet()));
    assembleTestResultSummary(enabledTestTypes, result, scriptInfosVoMap, resultVo);
    return resultVo;
  }

  /**
   * Assembles execution APIs result information from test data.
   * <p>
   * Processes test APIs data and creates comprehensive result information including
   * test counts, progress tracking, and detailed result analysis for each API.
   * <p>
   * Handles different test types (functional, performance, stability) and provides
   * aggregated statistics for reporting purposes.
   *
   * @param result the result object to populate
   * @param testApis the test APIs count data
   */
  private void assembleExecApisResultInfo(ExecApisResultInfo result, ApisTestCount testApis) {
    result.setTestApis(testApis);

    // Note: serviceId is cannot be redundant, changes will occur after movement apis
    List<Long> enabledTestApiIds = testApis.getEnabledTestApiIds().values().stream()
        .flatMap(Collection::stream).toList().stream().distinct().toList();

    Map<Long, List<ExecTestResultInfo>> apisTestResultMap =
        execTestResultInfoRepo.findByScriptSourceIdInAndScriptSource(
                enabledTestApiIds, ScriptSource.API).stream()
            .collect(Collectors.groupingBy(ExecTestResultInfo::getScriptSourceId));

    List<ExecResultSummary> allApiResultInfos = assembleApisTestResultInfos(
        testApis.getAllApis(), enabledTestApiIds, apisTestResultMap);

    testApis.setAllApis(isUserAction() ? null : testApis.getAllApis() /* Door by report job*/);
    result.setTestResultInfos(allApiResultInfos);

    TestResultCount testResult = new TestResultCount();
    testResult.setTotalNum(testApis.getTotalApisNum());
    testResult.setEnabledTestNum(enabledTestApiIds.size());
    testResult.setTestedNum(apisTestResultMap.size());
    testResult.setUnTestedNum(
        testResult.getEnabledTestNum() - testResult.getTestedNum());
    testResult.setTestPassedNum(
        allApiResultInfos.stream().filter(x -> x.isEnabledTest() && x.isPassed()).count());
    testResult.setTestUnPassedNum(
        testResult.getEnabledTestNum() - testResult.getTestPassedNum());
    result.setTestResultCount(testResult);

    Progress progress = new Progress().setTotal(testResult.getEnabledTestNum())
        .setCompleted(testResult.getTestPassedNum());
    result.setProgress(progress);
  }

  /**
   * Assembles APIs test result information from raw data.
   * <p>
   * Processes raw APIs information and test result data to create comprehensive
   * result summaries for each API. Handles different test types and their results.
   *
   * @param allApisInfos the list of all APIs information
   * @param enabledTestApiIds the list of enabled test API IDs
   * @param apisTestResultMap the map of API test results
   * @return List of ExecResultSummary objects
   */
  private static List<ExecResultSummary> assembleApisTestResultInfos(
      List<ApisInfo> allApisInfos, List<Long> enabledTestApiIds,
      Map<Long, List<ExecTestResultInfo>> apisTestResultMap) {

    return allApisInfos.stream().map(x -> {
      ExecResultSummary apiResultInfo = new ExecResultSummary();
      apiResultInfo.setId(x.getId()).setSummary(x.getSummary()).setMethod(x.getMethod())
          .setUrl(x.getUrl()).setStatus(x.getStatus()).setTestFunc(x.getTestFunc())
          .setTestPerf(x.getTestPerf()).setTestStability(x.getTestStability());
      apiResultInfo.setEnabledTest(enabledTestApiIds.contains(x.getId()));
      List<ExecTestResultInfo> testResults = apisTestResultMap.get(x.getId());
      if (isEmpty(testResults)) {
        return apiResultInfo;
      }
      for (ExecTestResultInfo testResult : testResults) {
        if (testResult.getScriptType().isFunctionalTesting()) {
          apiResultInfo.setFuncTestPassed(testResult.isPassed())
              .setFuncTestFailureMessage(testResult.getFailureMessage());
        } else if (testResult.getScriptType().isPerformanceTesting()) {
          apiResultInfo.setPerfTestPassed(testResult.isPassed())
              .setPerfTestFailureMessage(testResult.getFailureMessage());
        } else if (testResult.getScriptType().isStabilityTesting()) {
          apiResultInfo.setStabilityTestPassed(testResult.isPassed())
              .setStabilityTestFailureMessage(testResult.getFailureMessage());
        }
      }
      return apiResultInfo;
    }).toList();
  }

  /**
   * Assembles execution scenario result information from test data.
   * <p>
   * Processes test scenarios data and creates comprehensive result information including
   * test counts, progress tracking, and detailed result analysis for each scenario.
   * <p>
   * Similar to assembleExecApisResultInfo but operates on scenario data instead of API data.
   *
   * @param result the result object to populate
   * @param testScenario the test scenario count data
   */
  private void assembleExecScenarioResultInfo(ExecScenarioResultInfo result,
      ScenarioTestCount testScenario) {
    result.setTestScenarios(testScenario);

    // Note: serviceId is cannot be redundant, changes will occur after movement apis
    List<Long> enabledTestScenarioIds = testScenario.getEnabledTestScenarioIds().values().stream()
        .flatMap(Collection::stream).toList().stream().distinct().toList();

    Map<Long, List<ExecTestResultInfo>> apisTestResultMap =
        execTestResultInfoRepo.findByScriptSourceIdInAndScriptSource(
                enabledTestScenarioIds, ScriptSource.SCENARIO).stream()
            .collect(Collectors.groupingBy(ExecTestResultInfo::getScriptSourceId));

    List<ExecResultSummary> allScenarioResultInfos = assembleScenarioTestResultInfos(
        testScenario.getAllScenarios(), enabledTestScenarioIds, apisTestResultMap);

    testScenario.setAllScenarios(
        isUserAction() ? null : testScenario.getAllScenarios() /* Door by report job*/);
    result.setTestResultInfos(allScenarioResultInfos);

    TestResultCount testResult = new TestResultCount();
    testResult.setTotalNum(testScenario.getTotalScenarioNum());
    testResult.setEnabledTestNum(enabledTestScenarioIds.size());
    testResult.setTestedNum(apisTestResultMap.size());
    testResult.setUnTestedNum(
        testResult.getEnabledTestNum() - testResult.getTestedNum());
    testResult.setTestPassedNum(
        allScenarioResultInfos.stream().filter(x -> x.isEnabledTest() && x.isPassed()).count());
    testResult.setTestUnPassedNum(
        testResult.getEnabledTestNum() - testResult.getTestPassedNum());
    result.setTestResultCount(testResult);

    Progress progress = new Progress().setTotal(testResult.getEnabledTestNum())
        .setCompleted(testResult.getTestPassedNum());
    result.setProgress(progress);
  }

  /**
   * Assembles scenario test result information from raw data.
   * <p>
   * Processes raw scenario information and test result data to create comprehensive
   * result summaries for each scenario. Handles different test types and their results.
   *
   * @param allScenarioInfos the list of all scenarios information
   * @param enabledTestScenarioIds the list of enabled test scenario IDs
   * @param scenarioTestResultMap the map of scenario test results
   * @return List of ExecResultSummary objects
   */
  private static List<ExecResultSummary> assembleScenarioTestResultInfos(
      List<ScenarioInfo> allScenarioInfos, List<Long> enabledTestScenarioIds,
      Map<Long, List<ExecTestResultInfo>> scenarioTestResultMap) {

    return allScenarioInfos.stream().map(x -> {
      ExecResultSummary apiResultInfo = new ExecResultSummary();
      apiResultInfo.setId(x.getId()).setSummary(x.getName());
      apiResultInfo.setEnabledTest(enabledTestScenarioIds.contains(x.getId()));
      List<ExecTestResultInfo> testResults = scenarioTestResultMap.get(x.getId());
      if (isEmpty(testResults)) {
        return apiResultInfo;
      }
      for (ExecTestResultInfo testResult : testResults) {
        if (testResult.getScriptType().isFunctionalTesting()) {
          apiResultInfo.setFuncTestPassed(testResult.isPassed())
              .setFuncTestFailureMessage(testResult.getFailureMessage());
        } else if (testResult.getScriptType().isPerformanceTesting()) {
          apiResultInfo.setPerfTestPassed(testResult.isPassed())
              .setPerfTestFailureMessage(testResult.getFailureMessage());
        } else if (testResult.getScriptType().isStabilityTesting()) {
          apiResultInfo.setStabilityTestPassed(testResult.isPassed())
              .setStabilityTestFailureMessage(testResult.getFailureMessage());
        }
      }
      return apiResultInfo;
    }).toList();
  }

  /**
   * Sets execution names for a collection of test results.
   * <p>
   * Looks up execution information and populates the execution name field
   * for each test result in the collection.
   *
   * @param results the collection of test results to update
   */
  private void setResultExecName(Collection<ExecTestResult> results) {
    Set<Long> execIds = results.stream().map(ExecTestResult::getExecId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Map<Long, IdAndName> execMap = execQuery.findInfoMap(execIds);
    for (ExecTestResult result : results) {
      if (execMap.containsKey(result.getExecId())) {
        result.setExecName(execMap.get(result.getExecId()).getName());
      }
    }
  }

  /**
   * Sets execution names for a collection of test case results.
   * <p>
   * Looks up execution information and populates the execution name field
   * for each test case result in the collection.
   *
   * @param results the collection of test case results to update
   */
  private void setCaseResultExecName(Collection<ExecTestCaseResult> results) {
    Set<Long> execIds = results.stream().map(ExecTestCaseResult::getExecId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Map<Long, IdAndName> execMap = execQuery.findInfoMap(execIds);
    for (ExecTestCaseResult result : results) {
      if (execMap.containsKey(result.getExecId())) {
        result.setExecName(execMap.get(result.getExecId()).getName());
      }
    }
  }

  /**
   * Sets execution by names for a collection of test results.
   * <p>
   * Looks up user information and populates the execution by name field
   * for each test result in the collection.
   *
   * @param results the collection of test results to update
   */
  private void setResultExecByName(Collection<ExecTestResult> results) {
    Set<Long> userIds = results.stream().map(ExecTestResult::getExecBy)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Map<Long, UserBase> userBaseMap = userManager.getUserBaseMap(userIds);
    for (ExecTestResult result : results) {
      if (userBaseMap.containsKey(result.getExecBy())) {
        result.setExecByName(userBaseMap.get(result.getExecBy()).getFullName());
      }
    }
  }

  /**
   * Sets execution by names for a collection of test case results.
   * <p>
   * Looks up user information and populates the execution by name field
   * for each test case result in the collection.
   *
   * @param results the collection of test case results to update
   */
  private void setCaseResultExecByName(Collection<ExecTestCaseResult> results) {
    Set<Long> userIds = results.stream().map(ExecTestCaseResult::getExecBy)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Map<Long, UserBase> userBaseMap = userManager.getUserBaseMap(userIds);
    for (ExecTestCaseResult result : results) {
      if (userBaseMap.containsKey(result.getExecBy())) {
        result.setExecByName(userBaseMap.get(result.getExecBy()).getFullName());
      }
    }
  }

  /**
   * Sets test result and name information for a single test result.
   * <p>
   * Populates execution name, executor name, and case results for API functional testing.
   * This method provides complete information enrichment for a single test result.
   *
   * @param result the test result to update
   */
  private void setTestResultAndName(ExecTestResult result) {
    setResultExecName(Collections.singleton(result));
    setResultExecByName(Collections.singleton(result));

    if (result.getScriptType().isFunctionalTesting() && result.getScriptSource().isApis()) {
      result.setCaseResults(execTestCaseResultRepo.findByApisId(result.getScriptSourceId()));
    }
  }
}
