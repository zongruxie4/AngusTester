package cloud.xcan.angus.core.tester.interfaces.exec;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestCaseResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
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

  @Operation(summary = "Query the execution test result.", operationId = "exec:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{execId}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> execResult(
      @Parameter(name = "execId", description = "Execution id", required = true) @PathVariable("execId") Long execId) {
    return ApiLocaleResult.success(execResultFacade.execResult(execId));
  }

  @Operation(summary = "Query the apis execution test result by script type.", operationId = "exec:apis:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/apis/{apiId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> apisResultByScriptType(
      @Parameter(name = "apiId", description = "Apis id", required = true) @PathVariable("apiId") Long apiId,
      @Parameter(name = "scriptType", description = "Script type", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(execResultFacade.apisResultByScriptType(apiId, scriptType));
  }

  @Operation(summary = "Query the execution test result of apis.", operationId = "exec:apis:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/apis/{apiId}/result")
  public ApiLocaleResult<ExecTestResultSummary> apisResult(
      @Parameter(name = "apiId", description = "Apis id", required = true) @PathVariable("apiId") Long apiId) {
    return ApiLocaleResult.success(execResultFacade.apisResult(apiId));
  }

  @Operation(summary = "Query the execution test result of service", operationId = "exec:service:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/service/{serviceId}/result")
  public ApiLocaleResult<ExecApisResultInfo> serviceApisResult(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.serviceApisResult(serviceId, dto));
  }

  @Operation(summary = "Query the execution apis test result of project", operationId = "exec:project:apis:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/apis/result")
  public ApiLocaleResult<ExecApisResultInfo> projectApisResult(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectApisResult(projectId, dto));
  }

  @Operation(summary = "Query the scenario execution test result by script type", operationId = "exec:scenario:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/scenario/{scenarioId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> scenarioResultByScriptType(
      @Parameter(name = "scenarioId", description = "Scenario id", required = true) @PathVariable("scenarioId") Long scenarioId,
      @Parameter(name = "scriptType", description = "Script type", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(
        execResultFacade.scenarioResultByScriptType(scenarioId, scriptType));
  }

  @Operation(summary = "Query the execution test result of scenario", operationId = "exec:scenario:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/scenario/{scenarioId}/result")
  public ApiLocaleResult<ExecTestResultSummary> scenarioResult(
      @Parameter(name = "scenarioId", description = "Scenario id", required = true) @PathVariable("scenarioId") Long scenarioId) {
    return ApiLocaleResult.success(execResultFacade.scenarioResult(scenarioId));
  }

  @Operation(summary = "Query the execution scenario test result of project", operationId = "exec:project:scenario:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/scenario/result")
  public ApiLocaleResult<ExecScenarioResultInfo> projectScenarioResult(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectScenarioResult(projectId, dto));
  }

  @Operation(summary = "Query the execution result of case", operationId = "exec:case:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/case/{caseId}/result")
  public ApiLocaleResult<ExecTestCaseResultDetailSummary> caseResult(
      @Parameter(name = "caseId", description = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(execResultFacade.caseResult(caseId));
  }
}
