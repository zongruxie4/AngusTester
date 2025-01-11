package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskSprintAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

@Api(tags = "TaskSprintAuth")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintAuthRest {

  @Resource
  private TaskSprintAuthFacade taskSprintAuthFacade;

  @ApiOperation(value = "Add the authorization of task sprint", nickname = "task:sprint:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Valid @RequestBody TaskSprintAuthAddDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.add(sprintId, dto));
  }

  @ApiOperation(value = "Replace the authorization of task sprint", nickname = "task:sprint:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Task sprint authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskSprintAuthReplaceDto dto) {
    taskSprintAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of task sprint", nickname = "task:sprint:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    taskSprintAuthFacade.enabled(sprintId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of task sprint", nickname = "task:sprint:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId) {
    return ApiLocaleResult.success(taskSprintAuthFacade.status(sprintId));
  }

  @ApiOperation(value = "Delete the authorization of task sprint", nickname = "task:sprint:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Task sprint authorization id", required = true) @PathVariable("id") Long id) {
    taskSprintAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of task sprint", nickname = "task:sprint:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<TaskSprintPermission>> userAuth(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(taskSprintAuthFacade.userAuth(sprintId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of task sprint", nickname = "task:sprint:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<TaskSprintAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult
        .success(taskSprintAuthFacade.currentUserAuth(sprintId, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of task sprint, the administrator permission is included", nickname = "task:sprint:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Task sprint id", required = true) @PathVariable("id") Long sprintId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Task sprint authorized permission", required = true) @PathVariable("authPermission") TaskSprintPermission permission) {
    taskSprintAuthFacade.authCheck(sprintId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of task sprint authorization", nickname = "task:sprint:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<TaskSprintAuthVo>> list(@Valid TaskSprintAuthFindDto dto) {
    return ApiLocaleResult.success(taskSprintAuthFacade.list(dto));
  }

}
