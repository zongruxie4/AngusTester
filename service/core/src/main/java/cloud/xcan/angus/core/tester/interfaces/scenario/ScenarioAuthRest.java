package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
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

@Tag(name = "Scenario Authorization", description = "Scenario Authorization Management API - Comprehensive access control system for managing data permissions, user roles, and security policies for test scenarios.")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioAuthRest {

  @Resource
  private ScenarioAuthFacade scenarioAuthFacade;

  @Operation(summary = "Create scenario authorization",
      description = "Create new authorization rules for scenario access control and permission management.",
      operationId = "scenario:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Scenario identifier for authorization setup", required = true) @PathVariable("id") Long scenarioId,
      @Valid @RequestBody ScenarioAuthAddDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.add(scenarioId, dto));
  }

  @Operation(summary = "Replace scenario authorization",
      description = "Replace existing authorization configuration with new permission settings.",
      operationId = "scenario:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Authorization record not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Authorization record identifier for replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScenarioAuthReplaceDto dto) {
    scenarioAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable scenario authorization",
      description = "Toggle authorization status to control access to the scenario.",
      operationId = "scenario:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authorization status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Scenario identifier for authorization control", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization status flag", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioAuthFacade.enabled(scenarioId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query scenario authorization status",
      description = "Retrieve current authorization status for the specified scenario.",
      operationId = "scenario:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Scenario identifier for status query", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioAuthFacade.status(scenarioId));
  }

  @Operation(summary = "Delete scenario authorization",
      description = "Remove authorization record and revoke associated permissions.",
      operationId = "scenario:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Authorization record identifier for deletion", required = true) @PathVariable("id") Long id) {
    scenarioAuthFacade.delete(id);
  }

  @Operation(summary = "Query user's scenario permissions",
      description = "Retrieve all permissions granted to a specific user for the scenario.",
      operationId = "scenario:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario or user not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScenarioPermission>> userAuth(
      @Parameter(name = "id", description = "Scenario identifier for permission query", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "userId", description = "User identifier for permission check", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(scenarioAuthFacade.userAuth(scenarioId, userId, admin));
  }

  @Operation(summary = "Query current user's scenario permissions",
      description = "Retrieve permissions for the currently authenticated user on the specified scenario.",
      operationId = "scenario:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScenarioAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Scenario identifier for current user permission query", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "admin", description = "Flag to include administrator permissions in query") Boolean admin) {
    return ApiLocaleResult.success(scenarioAuthFacade.currentUserAuth(scenarioId, admin));
  }

  @Operation(summary = "Check user authorization permission",
      description = "Verify if a user has specific permission or administrator access to the scenario.",
      operationId = "scenario:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authorization check completed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Scenario identifier for permission verification", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "userId", description = "User identifier for permission verification", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Specific permission to verify", required = true)
      @PathVariable("authPermission") ScenarioPermission permission) {
    scenarioAuthFacade.authCheck(scenarioId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query scenario authorization list",
      description = "Retrieve paginated list of all authorization records with filtering capabilities.",
      operationId = "scenario:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authorization list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScenarioAuthVo>> list(
      @Valid @ParameterObject ScenarioAuthFindDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.list(dto));
  }

}
