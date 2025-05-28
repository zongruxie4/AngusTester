package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskSprintAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthVo;
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

@Tag(name = "TaskSprintAuth", description = "Task Sprint Authorization Management - Unified entry for managing data access permissions of task sprint.")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintAuthRest {

  @Resource
  private TaskSprintAuthFacade taskSprintAuthFacade;

  @Operation(summary = "Add the authorization of task sprint", operationId = "task:sprint:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Valid @RequestBody TaskSprintAuthAddDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.add(sprintId, dto));
  }

  @Operation(summary = "Replace the authorization of task sprint", operationId = "task:sprint:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Task sprint authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskSprintAuthReplaceDto dto) {
    taskSprintAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of task sprint", operationId = "task:sprint:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    taskSprintAuthFacade.enabled(sprintId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of task sprint", operationId = "task:sprint:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId) {
    return ApiLocaleResult.success(taskSprintAuthFacade.status(sprintId));
  }

  @Operation(summary = "Delete the authorization of task sprint", operationId = "task:sprint:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Task sprint authorization id", required = true) @PathVariable("id") Long id) {
    taskSprintAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permission of task sprint", operationId = "task:sprint:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<TaskSprintPermission>> userAuth(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(taskSprintAuthFacade.userAuth(sprintId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permission of task sprint", operationId = "task:sprint:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<TaskSprintAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult
        .success(taskSprintAuthFacade.currentUserAuth(sprintId, admin));
  }

  @Operation(summary = "Check the user authorization permission of task sprint, the administrator permission is included", operationId = "task:sprint:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Task sprint authorized permission", required = true) @PathVariable("authPermission") TaskSprintPermission permission) {
    taskSprintAuthFacade.authCheck(sprintId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of task sprint authorization", operationId = "task:sprint:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<TaskSprintAuthVo>> list(
      @Valid @ParameterObject TaskSprintAuthFindDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.list(dto));
  }

}
