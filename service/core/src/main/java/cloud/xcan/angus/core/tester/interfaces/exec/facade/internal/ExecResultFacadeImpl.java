package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecResultAssembler.assembleExecTestResultVo0;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecResultAssembler.toExecCaseResultVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecResultAssembler.toExecResultVo;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecTestResultQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResult;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
  public ExecTestResultDetail execResult(Long execId) {
    cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult testResult = execTestResultQuery.execTestResult(execId);
    return isNull(testResult) ? null : toExecResultVo(testResult,
        getScriptInfosVoMap(Set.of(testResult.getScriptId())).get(testResult.getScriptId()));
  }

  @Override
  public ExecTestResultDetail apisResultByScriptType(Long scriptSourceId, String scriptType) {
    cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult testResult = execTestResultQuery.resultByScriptType(scriptSourceId,
        ScriptType.valueOf(scriptType));
    return isNull(testResult) ? null : toExecResultVo(testResult,
        getScriptInfosVoMap(Set.of(testResult.getScriptId())).get(testResult.getScriptId()));
  }

  @Override
  public ExecTestResult apisResult(Long apiId) {
    List<TestType> enabledTestTypes = apisTestQuery.findEnabledTestTypes(apiId);
    return assembleExecTestResultVo(apiId, enabledTestTypes);
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
  public ExecTestResultDetail scenarioResultByScriptType(Long scriptSourceId, String scriptType) {
    return apisResultByScriptType(scriptSourceId, scriptType);
  }

  @Override
  public ExecTestResult scenarioResult(Long scenarioId) {
    List<TestType> enabledTestTypes = scenarioTestQuery.findEnabledTestTypes(scenarioId);
    return assembleExecTestResultVo(scenarioId, enabledTestTypes);
  }

  @Override
  public ExecScenarioResultInfo projectScenarioResult(Long projectId, OrgAndDateFilterDto dto) {
    return execTestResultQuery.projectScenarioResult(projectId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ExecTestCaseResultDetail caseResult(Long caseId) {
    ExecTestCaseResult result = execTestResultQuery.caseResult(caseId);
    return isEmpty(result) ? null : toExecCaseResultVo(result, null);
  }

  @Override
  public ExecTestResult assembleExecTestResultVo(Long scriptSourceId,
      List<TestType> enabledTestTypes) {
    ExecTestResult resultVo = new ExecTestResult();
    List<cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult> result = execTestResultQuery.result(scriptSourceId);
    Map<Long, ScriptInfo> scriptInfosVoMap = getScriptInfosVoMap(
        result.stream().map(cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult::getScriptId).collect(Collectors.toSet()));
    assembleExecTestResultVo0(enabledTestTypes, result, scriptInfosVoMap, resultVo);
    return resultVo;
  }

  @NotNull
  private Map<Long, ScriptInfo> getScriptInfosVoMap(Set<Long> scriptIds) {
    Map<Long, ScriptInfo> scriptVoMap = new HashMap<>();
    if (isEmpty(scriptIds)) {
      return scriptVoMap;
    }
    try {
      scriptVoMap = scriptQuery.infos(new HashSet<>(scriptIds))
          .stream().collect(Collectors.toMap(ScriptInfo::getId, x -> x));
    } catch (Exception e) {
      log.warn("Join script infos failed.", e);
      // 404, script deleted
    }
    return scriptVoMap;
  }

}
