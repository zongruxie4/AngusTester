package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResult;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import java.util.List;

public interface ExecResultFacade {

  ExecTestResultDetail execResult(Long execId);

  ExecTestResultDetail apisResultByScriptType(Long apiId, String scriptType);

  ExecTestResult apisResult(Long apiId);

  ExecApisResultInfo serviceApisResult(Long serviceId, OrgAndDateFilterDto dto);

  ExecApisResultInfo projectApisResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestResultDetail scenarioResultByScriptType(Long scenarioId, String scriptType);

  ExecTestResult scenarioResult(Long scenarioId);

  ExecScenarioResultInfo projectScenarioResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestCaseResultDetail caseResult(Long caseId);

  ExecTestResult assembleExecTestResultVo(Long scriptSourceId,
      List<TestType> enabledTestTypes);
}
