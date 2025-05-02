package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestCaseResult;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecTestResult;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.time.LocalDateTime;
import java.util.List;

public interface ExecTestResultQuery {

  ExecTestResult execTestResult(Long execId);

  ExecTestResult resultByScriptType(Long apiId, ScriptType scriptType);

  List<ExecTestResult> result(Long apiId);

  ExecApisResultInfo serviceApisResult(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ExecApisResultInfo projectApisResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ExecScenarioResultInfo projectScenarioResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ExecTestCaseResult caseResult(Long caseId);

  ExecTestResultSummary assembleExecTestResultSummary(Long scriptSourceId,
      List<TestType> enabledTestTypes);
}
