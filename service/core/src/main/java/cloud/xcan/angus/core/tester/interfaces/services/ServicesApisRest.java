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

@Tag(name = "ServicesApis", description = "Service apis and parameters management - "
    + "Configure global parameters (e.g., authentication headers, versioning) for all APIs under a service")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesApisRest {

  @Resource
  private ApisFacade apisFacade;

  @Operation(summary = "Add api parameters",
      description = "Add the parameters of apis, override parameter value if it exists",
      operationId = "apis:parameter:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> addParameters(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
    apisFacade.addParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update api parameters", operationId = "apis:parameter:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> updateParameters(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
    apisFacade.updateParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete api parameters", operationId = "apis:parameter:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> deleteParameters(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Parameter names", required = true) @RequestParam(value = "names") List<String> names,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteParameters(serviceId, dto, names);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable api parameters", operationId = "apis:parameter:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enable or disable successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter/enabled")
  public ApiLocaleResult<?> enableParameters(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Parameter names", required = true) @RequestParam(value = "names") List<String> names,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled or Disabled", required = true) @RequestParam(value = "enabled") Boolean enabled,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.enableParameters(serviceId, dto, names, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update api authentication", operationId = "apis:authentication:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enable or disable successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/authentication")
  public ApiLocaleResult<?> updateAuth(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto, @RequestBody SecurityScheme authentication) {
    apisFacade.updateAuth(serviceId, dto, authentication);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update the current api server configuration", operationId = "apis:server:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enable or disable successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/server")
  public ApiLocaleResult<?> updateServer(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisScopeDto dto, @RequestBody Server server) {
    apisFacade.updateServer(serviceId, dto, server);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Add api variable references", operationId = "apis:variable:reference:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> addVariableReference(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Variable names", required = true) @RequestParam(value = "names", required = true) List<String> variableNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.addVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete api variable references", operationId = "apis:variable:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> deleteVariableReference(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Variable names", required = true) @RequestParam(value = "names", required = true) List<String> variableNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Add api dataset reference", operationId = "apis:dataset:reference:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> addDatasetReference(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Dataset names", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.addDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete api dataset references", operationId = "apis:dataset:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> deleteDatasetReference(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @Parameter(name = "names", description = "Dataset names", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames,
      @Valid @ParameterObject ServiceApisScopeDto dto) {
    apisFacade.deleteDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of HTTP and WebSocket apis", operationId = "services:apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{serviceId}/apis")
  public ApiLocaleResult<PageResult<ServicesApisInfoListVo>> listApis(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid @ParameterObject ServiceApisFindDto dto) {
    return ApiLocaleResult.success(apisFacade.listApis(serviceId, dto));
  }

}
