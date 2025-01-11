package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

@Api(tags = "ScenarioTest")
@Validated
@RestController
@RequestMapping("/api/v1")
public class ScenarioTestRest {

  @Resource
  private ScenarioTestFacade scenarioTestFacade;

  @ApiOperation(value =
      "Enable or disable the functionality, performance, stability testing of scenario."
          + "After enabled, the test will be marked as a mandatory activity and the results will be included in the performance analysis.", nickname = "scenario:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotEmpty @ApiParam(value = "Apis test type, allowable values: PERFORMANCE, FUNCTIONAL, STABILITY", allowableValues = "PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioTestFacade.testEnabled(scenarioId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Find enabled functionality, performance, stability testing type of scenario.", nickname = "scenario:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long scenario) {
    return ApiLocaleResult.success(scenarioTestFacade.testEnabledFind(scenario));
  }

  @ApiOperation(value = "The testing scenario summary the functionality, performance, stability testing of project", nickname = "project:test:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/scenario/count")
  public ApiLocaleResult<ScenarioTestCount> countProjectTestScenarios(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(scenarioTestFacade.countProjectTestScenarios(projectId, dto));
  }

  @ApiOperation(value = "Configure and generate functionality, performance, stability testing tasks of scenario, which will override the configuration when the task exists", nickname = "scenario:test:task:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Configure and generate successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/test/task/{taskSprintId}/generate")
  public ApiLocaleResult<?> testTaskGenerate(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @ApiParam(name = "taskSprintId", value = "Task sprint id, it is required for agile project management") @RequestParam(value = "taskSprintId", required = false) Long taskSprintId,
      @Valid @NotEmpty @RequestBody Set<ScenarioTestTaskGenerateDto> dto) {
    scenarioTestFacade.testTaskGenerate(scenarioId, taskSprintId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the existing functionality, performance, stability testing tasks of scenario", nickname = "scenario:test:task:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Restarted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/scenario/{id}/test/task/restart")
  public ApiLocaleResult<?> testTaskRestart(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    scenarioTestFacade.testTaskRestart(scenarioId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reopen the existing functionality, performance, stability testing tasks of scenario", nickname = "scenario:test:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Reopened successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/scenario/{id}/test/task/reopen")
  public ApiLocaleResult<?> testTaskReopen(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    scenarioTestFacade.testTaskReopen(scenarioId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the functionality, performance, stability testing tasks of scenario", nickname = "scenario:test:task:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/scenario/{id}/test/task")
  public void testTaskDelete(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @Valid @NotEmpty @RequestParam("testTypes") HashSet<TestType> testTypes) {
    scenarioTestFacade.testTaskDelete(scenarioId, testTypes);
  }

  @ApiOperation(value = "Query all server configurations of the scenario", nickname = "scenario:test:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/scenario/{id}/test/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.serverList(scenarioId));
  }

  @ApiOperation(value = "Create the scenario testing execution. Note: Only HTTP servers is supported", nickname = "scenario:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/scenario/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @RequestBody @Nullable List<Server> servers) {
    scenarioTestFacade.testExecAdd(scenarioId, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the test results of scenario", nickname = "scenario:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/scenario/{id}/test/result")
  public ApiLocaleResult<TestResultDetailVo> testResult(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioTestFacade.testResult(scenarioId));
  }

}
