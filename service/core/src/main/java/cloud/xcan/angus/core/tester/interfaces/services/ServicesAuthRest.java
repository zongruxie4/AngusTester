package cloud.xcan.angus.core.tester.interfaces.services;


import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ServicesAuth", description = "Services Authorization Management - Unified entry for managing data access permissions of api service")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesAuthRest {

  @Resource
  private ServicesAuthFacade servicesAuthFacade;

  @Operation(summary = "Add the authorization of services", operationId = "services:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @RequestBody ServicesAddAuthDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.add(projectId, dto));
  }

  @Operation(summary = "Replace the authorization of services", operationId = "services:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "services authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ServicesAuthReplaceDto dto) {
    servicesAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the authorization of services", operationId = "services:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "services authorization id", required = true) @PathVariable("id") Long id) {
    servicesAuthFacade.delete(id);
  }

  @Operation(summary = "Enable or disable the authorization of services", operationId = "services:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.enabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of services", operationId = "services:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId) {
    return ApiLocaleResult.success(servicesAuthFacade.status(projectId));
  }

  @Operation(summary = "Enable or disable the authorization of services apis", operationId = "services:apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/apis/auth/enabled")
  public ApiLocaleResult<?> apisEnabled(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.apisEnabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the user authorization permission of services and throw 404 when services doesn't exist", operationId = "services:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ServicesPermission>> userAuth(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(servicesAuthFacade.userAuth(projectId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permission of services and throw 404 when services doesn't exist", operationId = "services:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ServiceAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(servicesAuthFacade.currentUserAuth(projectId, admin));
  }

  @Operation(summary = "Check the user authorization permission of services, the administrator permission is included", operationId = "services:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "services id", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Services authorized permission", required = true) @PathVariable("authPermission") ServicesPermission authPermission) {
    servicesAuthFacade.authCheck(projectId, authPermission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of services authorization", operationId = "services:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ServicesAuthVo>> list(
      @Valid @ParameterObject ServicesAuthFindDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.list(dto));
  }

}
