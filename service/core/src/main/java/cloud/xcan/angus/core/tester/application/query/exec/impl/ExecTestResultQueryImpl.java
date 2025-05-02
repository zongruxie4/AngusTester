package cloud.xcan.angus.core.tester.application.query.exec.impl;

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
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResultRepo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResultInfoRepo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResultRepo;
import cloud.xcan.angus.model.apis.ApisInfo;
import cloud.xcan.angus.model.scenario.ScenarioInfo;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.ScriptSource;
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
  private UserManager userManager;

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

  private void assembleExecApisResultInfo(ExecApisResultInfo result, ApisTestCount testApis) {
    result.setTestApis(testApis);

    // Note: serviceId is cannot be redundant, changes will occur after movement apis
    List<Long> enabledTestApiIds = testApis.getEnabledTestApiIds().values().stream()
        .flatMap(Collection::stream).toList().stream().distinct().collect(Collectors.toList());

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
    }).collect(Collectors.toList());
  }

  private void assembleExecScenarioResultInfo(ExecScenarioResultInfo result,
      ScenarioTestCount testScenario) {
    result.setTestScenarios(testScenario);

    // Note: serviceId is cannot be redundant, changes will occur after movement apis
    List<Long> enabledTestScenarioIds = testScenario.getEnabledTestScenarioIds().values().stream()
        .flatMap(Collection::stream).toList().stream().distinct().collect(Collectors.toList());

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
    }).collect(Collectors.toList());
  }

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

  private void setTestResultAndName(ExecTestResult result) {
    setResultExecName(Collections.singleton(result));
    setResultExecByName(Collections.singleton(result));

    if (result.getScriptType().isFunctionalTesting() && result.getScriptSource().isApis()) {
      result.setCaseResults(execTestCaseResultRepo.findByApisId(result.getScriptSourceId()));
    }
  }
}
