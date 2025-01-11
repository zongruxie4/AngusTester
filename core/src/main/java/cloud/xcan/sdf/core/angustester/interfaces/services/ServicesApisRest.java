package cloud.xcan.sdf.core.angustester.interfaces.services;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisScopeDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

@Api(tags = "ServicesApis")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesApisRest {

  @Resource
  private ApisFacade apisFacade;

  @ApiOperation(value = "Add apis parameters, override parameter value if it exists", nickname = "apis:parameter:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> addParameters(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<Parameter> parameters) {
    apisFacade.addParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Update apis parameters", nickname = "apis:parameter:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> updateParameters(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<Parameter> parameters) {
    apisFacade.updateParameters(serviceId, dto, parameters);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete apis parameters", nickname = "apis:parameter:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/parameter")
  public ApiLocaleResult<?> deleteParameters(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Parameter names", required = true) @RequestParam(value = "names") List<String> names) {
    apisFacade.deleteParameters(serviceId, dto, names);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable apis parameters", nickname = "apis:parameter:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enable or disable successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/parameter/enabled")
  public ApiLocaleResult<?> enableParameters(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Parameter names", required = true) @RequestParam(value = "names") List<String> names,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisFacade.enableParameters(serviceId, dto, names, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Update apis authentication", nickname = "apis:parameter:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enable or disable successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/authentication")
  public ApiLocaleResult<?> updateAuth(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto, @RequestBody SecurityScheme authentication) {
    apisFacade.updateAuth(serviceId, dto, authentication);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Update apis current server", nickname = "apis:server:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enable or disable successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{serviceId}/apis/server")
  public ApiLocaleResult<?> updateServer(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto, @RequestBody Server server) {
    apisFacade.updateServer(serviceId, dto, server);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Add apis reference variables", nickname = "apis:variable:reference:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> addVariableReference(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Variable names", required = true) @RequestParam(value = "names", required = true) List<String> variableNames) {
    apisFacade.addVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete apis reference variables", nickname = "apis:variable:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/variable/reference")
  public ApiLocaleResult<?> deleteVariableReference(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Variable names", required = true) @RequestParam(value = "names", required = true) List<String> variableNames) {
    apisFacade.deleteVariableReference(serviceId, dto, variableNames);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Add apis reference datasets", nickname = "apis:dataset:reference:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> addDatasetReference(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Dataset names", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames) {
    apisFacade.addDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete apis reference datasets", nickname = "apis:dataset:reference:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/apis/dataset/reference")
  public ApiLocaleResult<?> deleteDatasetReference(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisScopeDto dto,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "names", value = "Dataset names", required = true) @RequestParam(value = "names", required = true) List<String> datasetNames) {
    apisFacade.deleteDatasetReference(serviceId, dto, datasetNames);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of http and websocket apis", nickname = "services:apis:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{serviceId}/apis")
  public ApiLocaleResult<PageResult<ServicesApisInfoListVo>> listApis(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServiceApisFindDto dto) {
    return ApiLocaleResult.success(apisFacade.listApis(serviceId, dto));
  }

  @ApiOperation(value = "Fulltext search the http and websocket apis", nickname = "services:apis:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{serviceId}/apis/search")
  public ApiLocaleResult<PageResult<ServicesApisInfoListVo>> searchApis(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Valid ServicesApisSearchDto dto) {
    return ApiLocaleResult.success(apisFacade.searchApis(serviceId, dto));
  }

}
