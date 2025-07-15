package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTestFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestTaskGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.ApiLocaleResult;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
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

@Tag(name = "ApisTest", description = "User Test Management & Results - Testing management with historical results tracking and test configuration management")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisTestRest {

  @Resource
  private ApisTestFacade apisTestFacade;

  @Operation(summary = "Enable or disable the testing of api",
      description = "After enabled, the test will be marked as a mandatory activity and the results will be included in the performance analysis",
      operationId = "apis:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @Parameter(name = "id", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @Parameter(description = "Apis test type, allowable values: PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled or Disabled", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisTestFacade.testEnabled(apisId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Find enabled testing type of api", operationId = "apis:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @Parameter(name = "id", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testEnabledFind(apisId));
  }

  @Operation(summary = "Configure and generate the testing scripts of api", operationId = "apis:test:script:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Configure and generate successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/script/generate")
  public ApiLocaleResult<?> scriptGenerate(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @RequestBody Set<ApisTestScriptGenerateDto> dto) {
    apisTestFacade.scriptGenerate(apisId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the testing scripts of api", operationId = "apis:test:script:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/test/script")
  public void scriptDelete(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "testTypes", description = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    apisTestFacade.scriptDelete(apisId, testTypes);
  }

  @Operation(summary = "Configure and generate testing tasks of api", operationId = "apis:test:task:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Configure and generate successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/task/generate")
  public ApiLocaleResult<?> taskGenerate(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "taskSprintId", description = "Task sprint id, it is required for agile project management") @RequestParam(value = "taskSprintId", required = false) Long taskSprintId,
      @Valid @RequestBody Set<ApisTestTaskGenerateDto> dto) {
    apisTestFacade.testTaskGenerate(apisId, taskSprintId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart the existing testing tasks of api", operationId = "apis:test:task:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restarted Successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/{id}/test/task/restart")
  public ApiLocaleResult<?> taskRetest(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId) {
    apisTestFacade.testTaskRetest(apisId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reopen the existing testing tasks of the api", operationId = "apis:test:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reopened successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/{id}/test/task/reopen")
  public ApiLocaleResult<?> testTaskReopen(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId) {
    apisTestFacade.testTaskReopen(apisId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the testing tasks of api", operationId = "apis:test:task:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/test/task")
  public void testTaskDelete(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "testTypes", description = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    apisTestFacade.testTaskDelete(apisId, testTypes);
  }

  @Operation(summary = "Create the testing execution", description = "If the script does not exist, create the script", operationId = "apis:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "testTypes", description = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisId, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create the testing execution", description = "If the script does not exist, create the script", operationId = "apis:test:exec:add:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "ids", description = "Apis ids", required = true) @RequestParam("id") HashSet<Long> apisIds,
      @Parameter(name = "testTypes", description = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisIds, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create the functionality case testing execution", description = "If the script does not exist, create the script", operationId = "apis:case:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/case/exec")
  public ApiLocaleResult<?> testCaseExecAdd(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "caseIds", description = "Case ids", required = true) @RequestParam("caseIds") LinkedHashSet<Long> caseIds) {
    apisTestFacade.testCaseExecAdd(apisId, caseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the test results detail of apis", operationId = "apis:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/test/result/detail")
  public ApiLocaleResult<TestResultDetailVo> testResultDetail(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testResultDetail(apisId));
  }

}
