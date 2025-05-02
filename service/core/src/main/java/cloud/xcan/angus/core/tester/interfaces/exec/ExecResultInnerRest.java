package cloud.xcan.angus.core.tester.interfaces.exec;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetail;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResult;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used by report generate job.
 */
@Tag(name = "ExecResultInner", description = "Internal Test Results API - Service internal access to test artifacts: services, interfaces, executions, scripts, scenarios")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec")
public class ExecResultInnerRest {

  @Resource
  private ExecResultFacade execResultFacade;

  @Operation(description = "Query the execution test result.", operationId = "exec:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{execId}/result")
  public ApiLocaleResult<ExecTestResultDetail> execResult(
      @Parameter(name = "execId", description = "Execution id", required = true) @PathVariable("execId") Long execId) {
    return ApiLocaleResult.success(execResultFacade.execResult(execId));
  }

  @Operation(description = "Query the apis execution test result by script type.", operationId = "exec:apis:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/apis/{apiId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetail> apisResultByScriptType(
      @Parameter(name = "apiId", description = "Apis id", required = true) @PathVariable("apiId") Long apiId,
      @Parameter(name = "scriptType", description = "Script type", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(execResultFacade.apisResultByScriptType(apiId, scriptType));
  }

  @Operation(description = "Query the execution test result of apis.", operationId = "exec:apis:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/apis/{apiId}/result")
  public ApiLocaleResult<ExecTestResult> apisResult(
      @Parameter(name = "apiId", description = "Apis id", required = true) @PathVariable("apiId") Long apiId) {
    return ApiLocaleResult.success(execResultFacade.apisResult(apiId));
  }

  @Operation(description = "Query the execution test result of service", operationId = "exec:service:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/service/{serviceId}/result")
  public ApiLocaleResult<ExecApisResultInfo> serviceApisResult(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.serviceApisResult(serviceId, dto));
  }

  @Operation(description = "Query the execution apis test result of project", operationId = "exec:project:apis:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/apis/result")
  public ApiLocaleResult<ExecApisResultInfo> projectApisResult(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectApisResult(projectId ,dto));
  }

  @Operation(description = "Query the scenario execution test result by script type", operationId = "exec:scenario:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/scenario/{scenarioId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetail> scenarioResultByScriptType(
      @Parameter(name = "scenarioId", description = "Scenario id", required = true) @PathVariable("scenarioId") Long scenarioId,
      @Parameter(name = "scriptType", description = "Script type", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(
        execResultFacade.scenarioResultByScriptType(scenarioId, scriptType));
  }

  @Operation(description = "Query the execution test result of scenario", operationId = "exec:scenario:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/scenario/{scenarioId}/result")
  public ApiLocaleResult<ExecTestResult> scenarioResult(
      @Parameter(name = "scenarioId", description = "Scenario id", required = true) @PathVariable("scenarioId") Long scenarioId) {
    return ApiLocaleResult.success(execResultFacade.scenarioResult(scenarioId));
  }

  @Operation(description = "Query the execution scenario test result of project", operationId = "exec:project:scenario:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/scenario/result")
  public ApiLocaleResult<ExecScenarioResultInfo> projectScenarioResult(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectScenarioResult(projectId, dto));
  }

  @Operation(description = "Query the execution result of case", operationId = "exec:case:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/case/{caseId}/result")
  public ApiLocaleResult<ExecTestCaseResultDetail> caseResult(
      @Parameter(name = "caseId", description = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(execResultFacade.caseResult(caseId));
  }
}
