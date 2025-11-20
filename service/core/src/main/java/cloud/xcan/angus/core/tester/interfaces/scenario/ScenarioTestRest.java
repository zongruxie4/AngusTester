package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Scenario Test", description = "Scenario Testing Management API - End-user interfaces for executing, configuring, and tracking custom test scenarios with result analysis")
@Validated
@RestController
@RequestMapping("/api/v1")
public class ScenarioTestRest {

  @Resource
  private ScenarioTestFacade scenarioTestFacade;

  @Operation(summary = "Enable or disable scenario testing",
      description = "Configure test types for scenario execution and include results in performance analysis",
      operationId = "scenario:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario testing configuration updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @Parameter(name = "id", description = "Scenario identifier for testing configuration", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotEmpty @Parameter(description = "Test types to enable: PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @Parameter(name = "enabled", description = "Testing status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioTestFacade.testEnabled(scenarioId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query enabled test types for scenario",
      description = "Retrieve all enabled test types configured for the specified scenario",
      operationId = "scenario:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled test types retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @Parameter(name = "id", description = "Scenario identifier for test type query", required = true) @PathVariable("id") Long scenario) {
    return ApiLocaleResult.success(scenarioTestFacade.testEnabledFind(scenario));
  }

  @Operation(summary = "Query project test scenario count",
      description = "Get count of test scenarios and their execution statistics for the project",
      operationId = "project:test:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project test scenario count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/scenario/count")
  public ApiLocaleResult<ScenarioTestCount> countProjectTestScenarios(
      @Parameter(name = "id", description = "Project identifier for test scenario count", required = true) @PathVariable("id") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(scenarioTestFacade.countProjectTestScenarios(projectId, dto));
  }

  @Operation(summary = "Query scenario server configurations",
      description = "Retrieve all server configurations associated with the scenario for testing",
      operationId = "scenario:test:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Server configurations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Server configurations not found")
  })
  @GetMapping("/scenario/{id}/test/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Scenario identifier for server configuration query", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.serverList(scenarioId));
  }

  @Operation(summary = "Create scenario test execution",
      description = "Initiate test execution for the scenario with optional server configurations. Note: Only HTTP servers are supported",
      operationId = "scenario:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test execution created successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "id", description = "Scenario identifier for test execution", required = true) @PathVariable("id") Long scenarioId,
      @RequestBody @Nullable List<Server> servers) {
    scenarioTestFacade.testExecAdd(scenarioId, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query scenario test results",
      description = "Retrieve test results and analysis for the specified scenario",
      operationId = "scenario:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test results retrieved successfully")})
  @GetMapping(value = "/scenario/{id}/test/result")
  public ApiLocaleResult<TestResultDetailVo> testResult(
      @Parameter(name = "id", description = "Scenario identifier for test result query", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.testResult(scenarioId));
  }

}
