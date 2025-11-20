package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTestFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "APIs Testing", description = "APIs Testing Management - APIs for user test management, historical results tracking, test configuration management, and execution control")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisTestRest {

  @Resource
  private ApisTestFacade apisTestFacade;

  @Operation(summary = "Enable or disable API testing",
      description = "Configure API testing status with mandatory activity marking and performance analysis integration",
      operationId = "apis:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API testing status updated successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @Parameter(name = "id", description = "API identifier for test configuration", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @Parameter(description = "API test types with values: PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @Parameter(name = "enabled", description = "Testing status flag for enable/disable control", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisTestFacade.testEnabled(apisId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get enabled API test types",
      description = "Retrieve enabled testing types for specific API with comprehensive test configuration information",
      operationId = "apis:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled API test types retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @Parameter(name = "id", description = "API identifier for test type query", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testEnabledFind(apisId));
  }

  @Operation(summary = "Generate API test scripts",
      description = "Configure and generate testing scripts for API with comprehensive test automation setup",
      operationId = "apis:test:script:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API test scripts generated successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/script/generate")
  public ApiLocaleResult<?> scriptGenerate(
      @Parameter(name = "id", description = "API identifier for script generation", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @RequestBody Set<ApisTestScriptGenerateDto> dto) {
    apisTestFacade.scriptGenerate(apisId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API test scripts",
      description = "Remove testing scripts for API with comprehensive cleanup and validation",
      operationId = "apis:test:script:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API test scripts deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/test/script")
  public void scriptDelete(
      @Parameter(name = "id", description = "API identifier for script deletion", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "testTypes", description = "Test types for targeted script deletion", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    apisTestFacade.scriptDelete(apisId, testTypes);
  }

  @Operation(summary = "Create API test execution",
      description = "Create testing execution for API with automatic script creation if not exists",
      operationId = "apis:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API test execution created successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "id", description = "API identifier for execution creation", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "testTypes", description = "Test types for execution configuration", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisId, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create batch API test execution",
      description = "Create testing execution for multiple APIs with automatic script creation if not exists",
      operationId = "apis:test:exec:add:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Batch API test execution created successfully"),
      @ApiResponse(responseCode = "404", description = "One or more APIs not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "ids", description = "API identifiers for batch execution creation", required = true) @RequestParam("id") HashSet<Long> apisIds,
      @Parameter(name = "testTypes", description = "Test types for batch execution configuration", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisIds, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create API functional test execution",
      description = "Create functionality case testing execution for API with automatic script creation if not exists",
      operationId = "apis:case:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test execution created successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/case/exec")
  public ApiLocaleResult<?> testCaseExecAdd(
      @Parameter(name = "id", description = "API identifier for functional test execution", required = true) @PathVariable("id") Long apisId,
      @Parameter(name = "caseIds", description = "Case identifiers for functional test execution", required = true) @RequestParam("caseIds") LinkedHashSet<Long> caseIds) {
    apisTestFacade.testCaseExecAdd(apisId, caseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get API test results",
      description = "Retrieve comprehensive test results for API with detailed analysis and performance metrics",
      operationId = "apis:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API test results retrieved successfully")})
  @GetMapping(value = "/{id}/test/result/detail")
  public ApiLocaleResult<TestResultDetailVo> testResultDetail(
      @Parameter(name = "id", description = "API identifier for test result query", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testResultDetail(apisId));
  }

}
