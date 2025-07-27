package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
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

@Tag(name = "API Authorization", description = "API Authorization Management - Comprehensive APIs for managing user access permissions, authorization controls, and security policies for API resources")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisAuthRest {

  @Resource
  private ApisAuthFacade apisAuthFacade;

  @Operation(summary = "Create API authorization", 
      description = "Create new authorization rules for API access control with comprehensive permission settings",
      operationId = "apis:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "API identifier for authorization configuration", required = true) @PathVariable("id") Long apiId,
      @Valid @RequestBody ApisAuthAddDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.add(apiId, dto));
  }

  @Operation(summary = "Replace API authorization", 
      description = "Update existing API authorization rules with complete new configuration and permission settings",
      operationId = "apis:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "API authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "API authorization identifier for replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAuthReplaceDto dto) {
    apisAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable API authorization", 
      description = "Toggle API authorization status with comprehensive access control management",
      operationId = "apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API authorization status updated successfully"),
      @ApiResponse(responseCode = "404", description = "API authorization not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "API identifier for authorization status update", required = true) @PathVariable("id") Long apiId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization status flag for enable/disable control", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisAuthFacade.enabled(apiId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get API authorization status", 
      description = "Retrieve current authorization status for specific API with comprehensive access control information",
      operationId = "apis:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API authorization not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "API identifier for authorization status query", required = true) @PathVariable("id") Long apiId) {
    return ApiLocaleResult.success(apisAuthFacade.status(apiId));
  }

  @Operation(summary = "Delete API authorization", 
      description = "Remove API authorization rules with proper cleanup and access control validation",
      operationId = "apis:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "API authorization identifier for deletion", required = true) @PathVariable("id") Long id) {
    apisAuthFacade.delete(id);
  }

  @Operation(summary = "Get user API permissions", 
      description = "Retrieve comprehensive user permissions for specific API with administrator privilege support",
      operationId = "apis:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User API permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API or user not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ApiPermission>> userAuth(
      @Parameter(name = "id", description = "API identifier for permission query", required = true) @PathVariable("id") Long apiId,
      @PathVariable("userId") @Parameter(name = "userId", description = "User identifier for permission query", required = true) Long userId,
      @Parameter(name = "admin", description = "Administrator privilege flag for enhanced permission query") Boolean admin) {
    return ApiLocaleResult.success(apisAuthFacade.userAuth(apiId, userId, admin));
  }

  @Operation(summary = "Get current user API permissions", 
      description = "Retrieve current user permissions for specific API with administrator privilege support",
      operationId = "apis:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user API permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ApisAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "API identifier for current user permission query", required = true) @PathVariable("id") Long apiId,
      @Parameter(name = "admin", description = "Administrator privilege flag for enhanced permission query") Boolean admin) {
    return ApiLocaleResult.success(apisAuthFacade.currentUserAuth(apiId, admin));
  }

  @Operation(summary = "Check user API authorization", 
      description = "Validate user authorization or administrator permission for specific API with comprehensive access control",
      operationId = "apis:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization validation successful")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "API identifier for authorization check", required = true) @PathVariable("id") Long apiId,
      @Parameter(name = "userId", description = "User identifier for authorization validation", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "API permission type for authorization validation", required = true) @PathVariable("authPermission") ApiPermission permission) {
    apisAuthFacade.authCheck(apiId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query API authorization list", 
      description = "Retrieve paginated list of API authorizations with comprehensive filtering and search options",
      operationId = "apis:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API authorization list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ApiAuthVo>> list(@Valid @ParameterObject ApisAuthFindDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.list(dto));
  }

}
