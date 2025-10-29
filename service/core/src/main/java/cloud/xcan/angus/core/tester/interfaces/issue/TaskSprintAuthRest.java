package cloud.xcan.angus.core.tester.interfaces.issue;


import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskSprintAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.auth.TaskSprintAuthVo;
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

@Tag(name = "Task Sprint Authorization", description = "Task Sprint Authorization Management API - Access control system for sprint-level permissions including user roles, data access rights, and administrative privileges")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintAuthRest {

  @Resource
  private TaskSprintAuthFacade taskSprintAuthFacade;

  @Operation(summary = "Grant sprint authorization to user", operationId = "task:sprint:auth:add", description = "Assign specific permissions to a user for a task sprint, enabling controlled access to sprint data and operations")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Sprint authorization granted successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task sprint identifier to grant authorization for", required = true) @PathVariable("id") Long sprintId,
      @Valid @RequestBody TaskSprintAuthAddDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.add(sprintId, dto));
  }

  @Operation(summary = "Update sprint authorization permissions", operationId = "task:sprint:auth:replace", description = "Modify existing authorization permissions for a user within a task sprint, updating access levels and role assignments")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint authorization updated successfully"),
      @ApiResponse(responseCode = "404", description = "Sprint authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Sprint authorization identifier to update", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskSprintAuthReplaceDto dto) {
    taskSprintAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable sprint authorization", operationId = "task:sprint:auth:enabled", description = "Activate or deactivate authorization system for a task sprint, controlling overall access to sprint data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint authorization status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Task sprint identifier to update authorization status for", required = true) @PathVariable("id") Long sprintId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enable or disable authorization system", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    taskSprintAuthFacade.enabled(sprintId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get sprint authorization status", operationId = "task:sprint:auth:status", description = "Retrieve the current authorization status for a task sprint to determine if access control is active")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Task sprint identifier to check authorization status for", required = true) @PathVariable("id") Long sprintId) {
    return ApiLocaleResult.success(taskSprintAuthFacade.status(sprintId));
  }

  @Operation(summary = "Revoke sprint authorization", operationId = "task:sprint:auth:delete", description = "Remove authorization permissions for a user from a task sprint, revoking all associated access rights")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Sprint authorization revoked successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Sprint authorization identifier to revoke", required = true) @PathVariable("id") Long id) {
    taskSprintAuthFacade.delete(id);
  }

  @Operation(summary = "Get user sprint permissions", operationId = "task:sprint:user:auth", description = "Retrieve all authorization permissions assigned to a specific user for a task sprint")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User sprint permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<TaskSprintPermission>> userAuth(
      @Parameter(name = "id", description = "Task sprint identifier to check permissions for", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "userId", description = "User identifier to retrieve permissions for", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Include administrator permissions in the query") Boolean admin) {
    return ApiLocaleResult.success(taskSprintAuthFacade.userAuth(sprintId, userId, admin));
  }

  @Operation(summary = "Get current user sprint permissions", operationId = "task:sprint:user:auth:current", description = "Retrieve authorization permissions for the currently authenticated user within a specific task sprint")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user sprint permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<TaskSprintAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Task sprint identifier to check current user permissions for", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "admin", description = "Include administrator permissions in the query") Boolean admin) {
    return ApiLocaleResult.success(taskSprintAuthFacade.currentUserAuth(sprintId, admin));
  }

  @Operation(summary = "Verify user sprint permission", operationId = "task:sprint:auth:check", description = "Validate that a specific user has the required authorization permission for a task sprint operation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User permission verified successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Task sprint identifier to verify permissions for", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "userId", description = "User identifier to verify permissions for", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Specific permission to verify", required = true)
      @PathVariable("authPermission") TaskSprintPermission permission) {
    taskSprintAuthFacade.authCheck(sprintId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get paginated sprint authorizations list", operationId = "task:sprint:auth:list", description = "Retrieve a paginated list of all sprint authorizations with filtering options for user, permission type, and status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint authorizations list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<TaskSprintAuthVo>> list(
      @Valid @ParameterObject TaskSprintAuthFindDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.list(dto));
  }

}
