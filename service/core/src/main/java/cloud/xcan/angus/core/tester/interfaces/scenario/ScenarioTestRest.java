package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
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
import java.util.Set;
import javax.annotation.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ScenarioTest", description = "User Scenario Testing - End-user interfaces for executing and tracking custom test scenarios.")
@Validated
@RestController
@RequestMapping("/api/v1")
public class ScenarioTestRest {

  @Resource
  private ScenarioTestFacade scenarioTestFacade;

  @Operation(description =
      "Enable or disable the functionality, performance, stability testing of scenario."
          + "After enabled, the test will be marked as a mandatory activity and the results will be included in the performance analysis.", operationId = "scenario:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @Parameter(name = "id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotEmpty @Parameter(description = "Apis test type, allowable values: PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioTestFacade.testEnabled(scenarioId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Find enabled functionality, performance, stability testing type of scenario.", operationId = "scenario:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @Parameter(name = "id", required = true) @PathVariable("id") Long scenario) {
    return ApiLocaleResult.success(scenarioTestFacade.testEnabledFind(scenario));
  }

  @Operation(description = "The testing scenario summary the functionality, performance, stability testing of project", operationId = "project:test:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/scenario/count")
  public ApiLocaleResult<ScenarioTestCount> countProjectTestScenarios(
      @Parameter(name = "id", required = true) @PathVariable("id") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(scenarioTestFacade.countProjectTestScenarios(projectId, dto));
  }

  @Operation(description = "Configure and generate functionality, performance, stability testing tasks of scenario, which will override the configuration when the task exists", operationId = "scenario:test:task:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Configure and generate successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/test/task/{taskSprintId}/generate")
  public ApiLocaleResult<?> testTaskGenerate(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "taskSprintId", description = "Task sprint id, it is required for agile project management") @RequestParam(value = "taskSprintId", required = false) Long taskSprintId,
      @Valid @NotEmpty @RequestBody Set<ScenarioTestTaskGenerateDto> dto) {
    scenarioTestFacade.testTaskGenerate(scenarioId, taskSprintId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Restart the existing functionality, performance, stability testing tasks of scenario", operationId = "scenario:test:task:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/scenario/{id}/test/task/restart")
  public ApiLocaleResult<?> testTaskRestart(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    scenarioTestFacade.testTaskRestart(scenarioId);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Reopen the existing functionality, performance, stability testing tasks of scenario", operationId = "scenario:test:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reopened successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/scenario/{id}/test/task/reopen")
  public ApiLocaleResult<?> testTaskReopen(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    scenarioTestFacade.testTaskReopen(scenarioId);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the functionality, performance, stability testing tasks of scenario", operationId = "scenario:test:task:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/scenario/{id}/test/task")
  public void testTaskDelete(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "testTypes", description = "Test type", required = true) @Valid @NotEmpty @RequestParam("testTypes") HashSet<TestType> testTypes) {
    scenarioTestFacade.testTaskDelete(scenarioId, testTypes);
  }

  @Operation(description = "Query all server configurations of the scenario", operationId = "scenario:test:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @GetMapping("/scenario/{id}/test/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.serverList(scenarioId));
  }

  @Operation(description = "Create the scenario testing execution. Note: Only HTTP servers is supported", operationId = "scenario:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @RequestBody @Nullable List<Server> servers) {
    scenarioTestFacade.testExecAdd(scenarioId, servers);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Query the test results of scenario", operationId = "scenario:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/scenario/{id}/test/result")
  public ApiLocaleResult<TestResultDetailVo> testResult(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.testResult(scenarioId));
  }

}
