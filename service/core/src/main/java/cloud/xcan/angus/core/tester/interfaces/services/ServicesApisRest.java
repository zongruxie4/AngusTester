package cloud.xcan.angus.core.tester.interfaces.services;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServiceApisScopeDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
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

@Tag(name = "Services APIs", description = "Service APIs and Parameters Management API - Configuration system for global parameters, authentication headers, and versioning across all APIs under a service")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesApisRest {

  @Resource
  private ApisFacade apisFacade;

  @Operation(summary = "Add API parameters",
      description = "Add global parameters to APIs with scope-based targeting and value override capability",
      operationId = "apis:parameter:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API parameters added successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> addParameters(
      @Parameter(name = "serviceId", description = "Service identifier for parameter configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
    apisFacade.addParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update API parameters",
      description = "Update existing API parameters with scope-based targeting and value modification",
      operationId = "apis:parameter:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API parameters updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> updateParameters(
      @Parameter(name = "serviceId", description = "Service identifier for parameter configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
    apisFacade.updateParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API parameters",
      description = "Remove API parameters by name with scope-based targeting",
      operationId = "apis:parameter:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API parameters deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> deleteParameters(
      @Parameter(name = "serviceId", description = "Service identifier for parameter configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Parameter names for deletion", required = true) @RequestParam(value = "names") List<String> names,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteParameters(serviceId, dto, names);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable API parameters",
      description = "Toggle parameter status for scope-based API parameter management",
      operationId = "apis:parameter:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API parameter status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter/enabled")
  public ApiLocaleResult<?> enableParameters(
      @Parameter(name = "serviceId", description = "Service identifier for parameter configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Parameter names for status update", required = true) @RequestParam(value = "names") List<String> names,
      @Valid @NotNull @Parameter(name = "enabled", description = "Parameter status flag", required = true) @RequestParam(value = "enabled") Boolean enabled,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.enableParameters(serviceId, dto, names, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update API authentication",
      description = "Configure authentication scheme for scope-based API access control",
      operationId = "apis:authentication:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API authentication updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/authentication")
  public ApiLocaleResult<?> updateAuth(
      @Parameter(name = "serviceId", description = "Service identifier for authentication configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto, @RequestBody SecurityScheme authentication) {
    apisFacade.updateAuth(serviceId, dto, authentication);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update API server configuration",
      description = "Configure server settings for scope-based API execution environment",
      operationId = "apis:server:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API server configuration updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/server")
  public ApiLocaleResult<?> updateServer(
      @Parameter(name = "serviceId", description = "Service identifier for server configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto, @RequestBody Server server) {
    apisFacade.updateServer(serviceId, dto, server);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Add API variable references",
      description = "Configure variable references for scope-based API parameter substitution",
      operationId = "apis:variable:reference:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API variable references added successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> addVariableReference(
      @Parameter(name = "serviceId", description = "Service identifier for variable configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Variable names for reference configuration", required = true) @RequestParam(value = "names", required = true) List<String> variableNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.addVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API variable references",
      description = "Remove variable references for scope-based API parameter management",
      operationId = "apis:variable:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API variable references deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> deleteVariableReference(
      @Parameter(name = "serviceId", description = "Service identifier for variable configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Variable names for reference removal", required = true) @RequestParam(value = "names", required = true) List<String> variableNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Add API dataset reference",
      description = "Configure dataset references for scope-based API data integration",
      operationId = "apis:dataset:reference:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API dataset references added successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> addDatasetReference(
      @Parameter(name = "serviceId", description = "Service identifier for dataset configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Dataset names for reference configuration", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.addDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API dataset references",
      description = "Remove dataset references for scope-based API data management",
      operationId = "apis:dataset:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API dataset references deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> deleteDatasetReference(
      @Parameter(name = "serviceId", description = "Service identifier for dataset configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Dataset names for reference removal", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query HTTP and WebSocket APIs list",
      description = "Retrieve paginated list of HTTP and WebSocket APIs with filtering capabilities",
      operationId = "services:apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs list retrieved successfully")})
  @GetMapping("/{serviceId}/apis")
  public ApiLocaleResult<PageResult<ServicesApisInfoListVo>> listApis(
      @Parameter(name = "serviceId", description = "Service identifier for APIs query", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisFindDto dto) {
    return ApiLocaleResult.success(apisFacade.listApis(serviceId, dto));
  }

}
