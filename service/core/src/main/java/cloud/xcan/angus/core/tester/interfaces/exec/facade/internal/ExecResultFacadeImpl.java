package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;

import static cloud.xcan.angus.core.tester.application.converter.ExecResultSummaryConverter.toTestCaseResultDetailSummary;
import static cloud.xcan.angus.core.tester.application.converter.ExecResultSummaryConverter.toTestResultDetailSummary;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecTestResultQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecResultFacadeImpl implements ExecResultFacade {

  @Resource
  private ExecTestResultQuery execTestResultQuery;

  @Resource
  private ApisTestQuery apisTestQuery;

  @Resource
  private ScenarioTestQuery scenarioTestQuery;

  @Resource
  private ScriptQuery scriptQuery;

  @Override
  public ExecTestResultDetailSummary execResult(Long execId) {
    ExecTestResult testResult = execTestResultQuery.execTestResult(execId);
    return isNull(testResult) ? null : toTestResultDetailSummary(testResult,
        scriptQuery.getScriptInfoMap(Set.of(testResult.getScriptId()))
            .get(testResult.getScriptId()));
  }

  @Override
  public ExecTestResultDetailSummary apisResultByScriptType(Long scriptSourceId,
      String scriptType) {
    ExecTestResult testResult = execTestResultQuery.resultByScriptType(scriptSourceId,
        ScriptType.valueOf(scriptType));
    return isNull(testResult) ? null : toTestResultDetailSummary(testResult,
        scriptQuery.getScriptInfoMap(Set.of(testResult.getScriptId()))
            .get(testResult.getScriptId()));
  }

  @Override
  public ExecTestResultSummary apisResult(Long apiId) {
    List<TestType> enabledTestTypes = apisTestQuery.findEnabledTestTypes(apiId);
    return execTestResultQuery.assembleExecTestResultSummary(apiId, enabledTestTypes);
  }

  @Override
  public ExecApisResultInfo serviceApisResult(Long serviceId, OrgAndDateFilterDto dto) {
    return execTestResultQuery.serviceApisResult(serviceId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ExecApisResultInfo projectApisResult(Long projectId, OrgAndDateFilterDto dto) {
    return execTestResultQuery.projectApisResult(projectId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ExecTestResultDetailSummary scenarioResultByScriptType(Long scriptSourceId,
      String scriptType) {
    return apisResultByScriptType(scriptSourceId, scriptType);
  }

  @Override
  public ExecTestResultSummary scenarioResult(Long scenarioId) {
    List<TestType> enabledTestTypes = scenarioTestQuery.findEnabledTestTypes(scenarioId);
    return execTestResultQuery.assembleExecTestResultSummary(scenarioId, enabledTestTypes);
  }

  @Override
  public ExecScenarioResultInfo projectScenarioResult(Long projectId, OrgAndDateFilterDto dto) {
    return execTestResultQuery.projectScenarioResult(projectId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ExecTestCaseResultDetailSummary caseResult(Long caseId) {
    ExecTestCaseResult result = execTestResultQuery.caseResult(caseId);
    return isEmpty(result) ? null : toTestCaseResultDetailSummary(result, null);
  }

}
