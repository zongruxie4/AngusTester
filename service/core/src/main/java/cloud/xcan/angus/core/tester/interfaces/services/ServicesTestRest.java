package cloud.xcan.angus.core.tester.interfaces.services;


import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesSchemaFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.services.ApisTestCount;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Services Test", description = "API Test and Analytics Management API - Configuration and analysis system for user-initiated API testing and result management")
@Validated
@RestController
@RequestMapping("/api/v1")
public class ServicesTestRest {

  @Resource
  private ServicesTestFacade servicesTestFacade;

  @Resource
  private ServicesSchemaFacade servicesSchemaFacade;

  @Operation(summary = "Enable or disable service API testing",
      description = "Configure mandatory testing activity for service APIs with performance analysis inclusion",
      operationId = "services:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service API testing status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @Parameter(name = "id", description = "Service identifier for testing configuration", required = true) @PathVariable("id") Long serviceId,
      @Valid @NotEmpty @Parameter(description = "API test types for configuration", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @Parameter(name = "enabled", description = "Testing status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesTestFacade.testEnabled(serviceId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service API testing count",
      description = "Retrieve API testing execution statistics for service performance analysis",
      operationId = "services:test:apis:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service API testing count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/services/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @Parameter(name = "id", description = "Service identifier for testing statistics query", required = true) @PathVariable("id") Long serviceId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countServiceTestApis(serviceId, dto));
  }

  @Operation(summary = "Query project API testing count",
      description = "Retrieve API testing execution statistics for project performance analysis",
      operationId = "project:test:apis:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project API testing count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @Parameter(name = "id", description = "Project identifier for testing statistics query", required = true) @PathVariable("id") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countProjectTestApis(projectId, dto));
  }

  @Operation(summary = "Configure and generate service testing scripts",
      description = "Create and configure testing scripts for service with test type support",
      operationId = "services:test:script:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service testing scripts generated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/test/script/generate")
  public ApiLocaleResult<?> scriptGenerate(
      @Parameter(name = "id", description = "Service identifier for script generation", required = true) @PathVariable("id") Long serviceId,
      @Valid @NotEmpty @RequestBody Set<ApisTestScriptGenerateDto> dto) {
    servicesTestFacade.scriptGenerate(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service testing scripts",
      description = "Remove testing scripts by test types for service management",
      operationId = "services:test:script:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service testing scripts deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/services/{id}/test/script")
  public void scriptDelete(
      @Parameter(name = "id", description = "Service identifier for script management", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "testTypes", description = "Test types for script deletion", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    servicesTestFacade.scriptDelete(serviceId, testTypes);
  }

  @Operation(summary = "Query all service server configurations",
      description = "Retrieve all server configurations for service testing environment",
      operationId = "services:test:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service server configurations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/services/{id}/test/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Service identifier for server query", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverList(serviceId, true));
  }

  @Operation(summary = "Create service API testing execution",
      description = "Create testing execution for service APIs with automatic script generation if not exists",
      operationId = "services:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service API testing execution created successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @Parameter(name = "id", description = "Service identifier for testing execution", required = true) @PathVariable("id") Long servicesId,
      @Parameter(name = "testTypes", description = "Test types for execution", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testExecAdd(servicesId, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create service smoke testing execution",
      description = "Create smoke testing execution for service APIs with automatic script generation if not exists",
      operationId = "services:smoke:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service smoke testing execution created successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/smoke/exec")
  public ApiLocaleResult<?> testSmokeExecAdd(
      @Parameter(name = "id", description = "Service identifier for smoke testing execution", required = true) @PathVariable("id") Long servicesId,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testSmokeExecAdd(servicesId, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Create service security testing execution",
      description = "Create security testing execution for service APIs with automatic script generation if not exists",
      operationId = "services:security:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service security testing execution created successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/security/exec")
  public ApiLocaleResult<?> testSecurityExecAdd(
      @Parameter(name = "id", description = "Service identifier for security testing execution", required = true) @PathVariable("id") Long servicesId,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testSecurityExecAdd(servicesId, servers);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service test results",
      description = "Retrieve test results for service performance analysis",
      operationId = "services:test:apis:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service test results retrieved successfully")})
  @GetMapping(value = "/services/{id}/test/result")
  public ApiLocaleResult<ExecApisResultInfo> testServiceResult(
      @Parameter(name = "id", description = "Service identifier for test results query", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(servicesTestFacade.testServiceResult(serviceId));
  }

  @Operation(summary = "Query project test results",
      description = "Retrieve test results for project performance analysis",
      operationId = "project:test:apis:result:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project test results retrieved successfully")})
  @GetMapping(value = "/project/{id}/test/result")
  public ApiLocaleResult<ExecApisResultInfo> testProjectResult(
      @Parameter(name = "id", description = "Project identifier for test results query", required = true) @PathVariable("id") Long projectId) {
    return ApiLocaleResult.success(servicesTestFacade.testProjectResult(projectId));
  }

}
