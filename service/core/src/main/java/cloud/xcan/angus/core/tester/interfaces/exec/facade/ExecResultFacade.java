package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;

public interface ExecResultFacade {

  ExecTestResultDetailSummary execResult(Long execId);

  ExecTestResultDetailSummary apisResultByScriptType(Long apiId, String scriptType);

  ExecTestResultSummary apisResult(Long apiId);

  ExecApisResultInfo serviceApisResult(Long serviceId, OrgAndDateFilterDto dto);

  ExecApisResultInfo projectApisResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestResultDetailSummary scenarioResultByScriptType(Long scenarioId, String scriptType);

  ExecTestResultSummary scenarioResult(Long scenarioId);

  ExecScenarioResultInfo projectScenarioResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestCaseResultDetailSummary caseResult(Long caseId);

}
