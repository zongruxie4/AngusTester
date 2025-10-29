package cloud.xcan.angus.core.tester.interfaces.issue;


import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.follow.TaskFollowDetailVo;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task Follow", description = "Task Follow Management API - Notification system for subscribing to task updates and receiving real-time alerts when followed tasks are modified")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskFollowRest {

  @Resource
  private TaskFollowFacade taskFollowFacade;

  @Operation(summary = "Follow task for notifications", operationId = "task:follow:add", description = "Subscribe to a specific task to receive notifications when the task is updated, assigned, or has status changes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task follow subscription created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task identifier to follow for notifications", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFollowFacade.add(id));
  }

  @Operation(summary = "Unfollow task notifications", operationId = "task:follow:cancel", description = "Unsubscribe from notifications for a specific task to stop receiving update alerts")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task follow subscription removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @Parameter(name = "id", description = "Task identifier to unfollow notifications for", required = true) @PathVariable("id") Long id) {
    taskFollowFacade.cancel(id);
  }

  @Operation(summary = "Unfollow all tasks in project", operationId = "task:follow:cancel:all", description = "Unsubscribe from notifications for all followed tasks within a specific project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All project task follows removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to unfollow all tasks from") Long projectId) {
    taskFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "Get paginated followed tasks list", operationId = "task:follow:list", description = "Retrieve a paginated list of followed tasks with detailed information including follow timestamps and task metadata")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Followed tasks list retrieved successfully")})
  @GetMapping("/follow")
  public ApiLocaleResult<PageResult<TaskFollowDetailVo>> list(
      @Valid @ParameterObject TaskFollowFindDto dto) {
    return ApiLocaleResult.success(taskFollowFacade.list(dto));
  }

  @Operation(summary = "Get followed tasks count by project", operationId = "task:follow:count", description = "Retrieve the total count of followed tasks within a specific project for statistical and dashboard purposes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Followed tasks count retrieved successfully")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to count followed tasks for") Long projectId) {
    return ApiLocaleResult.success(taskFollowFacade.count(projectId));
  }
}
