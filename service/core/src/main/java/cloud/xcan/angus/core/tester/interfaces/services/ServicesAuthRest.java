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

@Tag(name = "Services Authorization", description = "Service Authorization Management API - Access control system for service permissions and user authorization management")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesAuthRest {

  @Resource
  private ServicesAuthFacade servicesAuthFacade;

  @Operation(summary = "Add service authorization",
      description = "Create new authorization configuration for service access control",
      operationId = "services:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Service authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Service identifier for authorization assignment", required = true) @PathVariable("id") Long projectId,
      @Valid @RequestBody ServicesAddAuthDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.add(projectId, dto));
  }

  @Operation(summary = "Replace service authorization",
      description = "Update existing authorization configuration with new permission settings",
      operationId = "services:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Authorization record identifier for replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ServicesAuthReplaceDto dto) {
    servicesAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service authorization",
      description = "Remove authorization configuration and revoke access permissions",
      operationId = "services:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Authorization record identifier for deletion", required = true) @PathVariable("id") Long id) {
    servicesAuthFacade.delete(id);
  }

  @Operation(summary = "Enable or disable service authorization",
      description = "Toggle authorization control status for service access management",
      operationId = "services:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service authorization status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Service identifier for authorization control", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization control status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.enabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service authorization status",
      description = "Retrieve current authorization control status for service access",
      operationId = "services:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Service identifier for status query", required = true) @PathVariable("id") Long projectId) {
    return ApiLocaleResult.success(servicesAuthFacade.status(projectId));
  }

  @Operation(summary = "Enable or disable service APIs authorization",
      description = "Toggle authorization control status for service APIs access management",
      operationId = "services:apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service APIs authorization status updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/apis/auth/enabled")
  public ApiLocaleResult<?> apisEnabled(
      @Parameter(name = "id", description = "Service identifier for APIs authorization control", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization control status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.apisEnabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query user service permissions",
      description = "Retrieve specific user's authorization permissions for service access",
      operationId = "services:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User service permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service or user not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ServicesPermission>> userAuth(
      @Parameter(name = "id", description = "Service identifier for permission query", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "userId", description = "User identifier for permission query", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(servicesAuthFacade.userAuth(projectId, userId, admin));
  }

  @Operation(summary = "Query current user service permissions",
      description = "Retrieve current authenticated user's authorization permissions for service access",
      operationId = "services:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user service permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ServiceAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Service identifier for current user permission query", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(servicesAuthFacade.currentUserAuth(projectId, admin));
  }

  @Operation(summary = "Check user service permission",
      description = "Verify specific user's authorization permission or administrator access for service",
      operationId = "services:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User permission verification successful")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Service identifier for permission verification", required = true) @PathVariable("id") Long projectId,
      @Parameter(name = "userId", description = "User identifier for permission verification", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Required permission level for verification", required = true) @PathVariable("authPermission") ServicesPermission authPermission) {
    servicesAuthFacade.authCheck(projectId, authPermission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service authorization list",
      description = "Retrieve paginated list of service authorization configurations with filtering capabilities",
      operationId = "services:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service authorization list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ServicesAuthVo>> list(
      @Valid @ParameterObject ServicesAuthFindDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.list(dto));
  }

}
