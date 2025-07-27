package cloud.xcan.angus.core.tester.interfaces.script;


import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthVo;
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

@Tag(name = "Script Authorization", description = "Script Authorization Management API - Comprehensive access control system for test script permissions and user authorization management.")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptAuthRest {

  @Resource
  private ScriptAuthFacade scriptAuthFacade;

  @Operation(summary = "Add script authorization",
      description = "Create new authorization configuration for script access control.",
      operationId = "script:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Script authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Script identifier for authorization assignment", required = true) @PathVariable("id") Long scriptId,
      @Valid @RequestBody ScriptAuthAddDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.add(scriptId, dto));
  }

  @Operation(summary = "Replace script authorization",
      description = "Update existing authorization configuration with new permission settings.",
      operationId = "script:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Script authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Authorization record identifier for replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScriptAuthReplaceDto dto) {
    scriptAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable script authorization",
      description = "Toggle authorization control status for script access management.",
      operationId = "script:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script authorization status updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Script identifier for authorization control", required = true) @PathVariable("id") Long scriptId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization control status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scriptAuthFacade.enabled(scriptId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query script authorization status",
      description = "Retrieve current authorization control status for script access.",
      operationId = "script:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Script identifier for status query", required = true) @PathVariable("id") Long scriptId) {
    return ApiLocaleResult.success(scriptAuthFacade.status(scriptId));
  }

  @Operation(summary = "Delete script authorization",
      description = "Remove authorization configuration and revoke access permissions.",
      operationId = "script:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Script authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Authorization record identifier for deletion", required = true) @PathVariable("id") Long id) {
    scriptAuthFacade.delete(id);
  }

  @Operation(summary = "Query user script permissions",
      description = "Retrieve specific user's authorization permissions for script access.",
      operationId = "script:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User script permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script or user not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScriptPermission>> userAuth(
      @Parameter(name = "id", description = "Script identifier for permission query", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "userId", description = "User identifier for permission query", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.userAuth(scriptId, userId, admin));
  }

  @Operation(summary = "Query current user script permissions",
      description = "Retrieve current authenticated user's authorization permissions for script access.",
      operationId = "script:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user script permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScriptAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Script identifier for current user permission query", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuth(scriptId, admin));
  }

  @Operation(summary = "Query current user permissions for multiple scripts",
      description = "Retrieve current authenticated user's authorization permissions for multiple scripts.",
      operationId = "script:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user script permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more scripts not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, ScriptAuthCurrentVo>> currentUserAuths(
      @Parameter(name = "scriptIds", description = "Script identifiers for batch permission query", required = true) @RequestParam(value = "scriptIds") @NotEmpty HashSet<Long> scriptIds,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuths(scriptIds, admin));
  }

  @Operation(summary = "Check user script permission",
      description = "Verify specific user's authorization permission or administrator access for script.",
      operationId = "script:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User permission verification successful")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Script identifier for permission verification", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "userId", description = "User identifier for permission verification", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Required permission level for verification", required = true) @PathVariable("authPermission") ScriptPermission permission) {
    scriptAuthFacade.authCheck(scriptId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query script authorization list",
      description = "Retrieve paginated list of script authorization configurations with filtering capabilities.",
      operationId = "script:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script authorization list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScriptAuthVo>> list(
      @Valid @ParameterObject ScriptAuthFindDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.list(dto));
  }

}
