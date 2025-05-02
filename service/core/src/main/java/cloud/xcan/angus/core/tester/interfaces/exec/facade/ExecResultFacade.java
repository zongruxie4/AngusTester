package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestCaseResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestResultVo;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import java.util.List;

public interface ExecResultFacade {

  ExecTestResultDetailVo execResult(Long execId);

  ExecTestResultDetailVo apisResultByScriptType(Long apiId, String scriptType);

  ExecTestResultVo apisResult(Long apiId);

  ExecApisResultInfo serviceApisResult(Long serviceId, OrgAndDateFilterDto dto);

  ExecApisResultInfo projectApisResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestResultDetailVo scenarioResultByScriptType(Long scenarioId, String scriptType);

  ExecTestResultVo scenarioResult(Long scenarioId);

  ExecScenarioResultInfo projectScenarioResult(Long projectId, OrgAndDateFilterDto dto);

  ExecTestCaseResultDetailVo caseResult(Long caseId);

  ExecTestResultVo assembleExecTestResultVo(Long scriptSourceId,
      List<TestType> enabledTestTypes);
}
