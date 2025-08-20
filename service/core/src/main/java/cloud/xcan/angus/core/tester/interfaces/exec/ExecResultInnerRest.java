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


@Tag(name = "Execution Result - Internal", description = "Internal Execution Result Management - Service internal access to test artifacts including services, interfaces, executions, scripts, and scenarios with result analysis")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec")
public class ExecResultInnerRest {

  @Resource
  private ExecResultFacade execResultFacade;

  @Operation(summary = "Get execution test result details", 
      description = "Retrieve comprehensive execution test result details for internal service integration",
      operationId = "exec:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution test result details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Execution test result not found")})
  @GetMapping(value = "/{execId}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> execResult(
      @Parameter(name = "execId", description = "Execution identifier for test result retrieval", required = true) @PathVariable("execId") Long execId) {
    return ApiLocaleResult.success(execResultFacade.execResult(execId));
  }

  @Operation(summary = "Get API execution test result by script type", 
      description = "Retrieve API execution test result filtered by specific script type for internal service integration",
      operationId = "exec:apis:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API execution test result retrieved successfully by script type"),
      @ApiResponse(responseCode = "404", description = "API execution test result not found")})
  @GetMapping(value = "/apis/{apiId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> apisResultByScriptType(
      @Parameter(name = "apiId", description = "API identifier for test result retrieval", required = true) @PathVariable("apiId") Long apiId,
      @Parameter(name = "scriptType", description = "Script type for result filtering", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(execResultFacade.apisResultByScriptType(apiId, scriptType));
  }

  @Operation(summary = "Get all API execution test results", 
      description = "Retrieve comprehensive API execution test results summary for internal service integration",
      operationId = "exec:apis:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All API execution test results retrieved successfully")})
  @GetMapping(value = "/apis/{apiId}/result")
  public ApiLocaleResult<ExecTestResultSummary> apisResult(
      @Parameter(name = "apiId", description = "API identifier for comprehensive test result retrieval", required = true) @PathVariable("apiId") Long apiId) {
    return ApiLocaleResult.success(execResultFacade.apisResult(apiId));
  }

  @Operation(summary = "Get service API execution test results", 
      description = "Retrieve service-level API execution test results with organizational and date filtering",
      operationId = "exec:service:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service API execution test results retrieved successfully")})
  @GetMapping(value = "/service/{serviceId}/result")
  public ApiLocaleResult<ExecApisResultInfo> serviceApisResult(
      @Parameter(name = "serviceId", description = "Service identifier for API test result retrieval", required = true) @PathVariable("serviceId") Long serviceId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.serviceApisResult(serviceId, dto));
  }

  @Operation(summary = "Get project API execution test results", 
      description = "Retrieve project-level API execution test results with organizational and date filtering",
      operationId = "exec:project:apis:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project API execution test results retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/apis/result")
  public ApiLocaleResult<ExecApisResultInfo> projectApisResult(
      @Parameter(name = "projectId", description = "Project identifier for API test result retrieval", required = true) @PathVariable("projectId") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectApisResult(projectId, dto));
  }

  @Operation(summary = "Get scenario execution test result by script type", 
      description = "Retrieve scenario execution test result filtered by specific script type for internal service integration",
      operationId = "exec:scenario:result:byScriptType:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario execution test result retrieved successfully by script type"),
      @ApiResponse(responseCode = "404", description = "Scenario execution test result not found")})
  @GetMapping(value = "/scenario/{scenarioId}/{scriptType}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> scenarioResultByScriptType(
      @Parameter(name = "scenarioId", description = "Scenario identifier for test result retrieval", required = true) @PathVariable("scenarioId") Long scenarioId,
      @Parameter(name = "scriptType", description = "Script type for result filtering", required = true) @PathVariable("scriptType") String scriptType) {
    return ApiLocaleResult.success(
        execResultFacade.scenarioResultByScriptType(scenarioId, scriptType));
  }

  @Operation(summary = "Get all scenario execution test results", 
      description = "Retrieve comprehensive scenario execution test results summary for internal service integration",
      operationId = "exec:scenario:result:all:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All scenario execution test results retrieved successfully")})
  @GetMapping(value = "/scenario/{scenarioId}/result")
  public ApiLocaleResult<ExecTestResultSummary> scenarioResult(
      @Parameter(name = "scenarioId", description = "Scenario identifier for comprehensive test result retrieval", required = true) @PathVariable("scenarioId") Long scenarioId) {
    return ApiLocaleResult.success(execResultFacade.scenarioResult(scenarioId));
  }

  @Operation(summary = "Get project scenario execution test results", 
      description = "Retrieve project-level scenario execution test results with organizational and date filtering",
      operationId = "exec:project:scenario:result:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project scenario execution test results retrieved successfully")})
  @GetMapping(value = "/project/{projectId}/scenario/result")
  public ApiLocaleResult<ExecScenarioResultInfo> projectScenarioResult(
      @Parameter(name = "projectId", description = "Project identifier for scenario test result retrieval", required = true) @PathVariable("projectId") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(execResultFacade.projectScenarioResult(projectId, dto));
  }

  @Operation(summary = "Get test case execution result details", 
      description = "Retrieve comprehensive test case execution result details for internal service integration",
      operationId = "exec:case:result:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case execution result details retrieved successfully")})
  @GetMapping(value = "/case/{caseId}/result")
  public ApiLocaleResult<ExecTestCaseResultDetailSummary> caseResult(
      @Parameter(name = "caseId", description = "Test case identifier for result detail retrieval", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(execResultFacade.caseResult(caseId));
  }
}
