package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
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

@Tag(name = "TaskFollow", description = "Task Watchlist/Subscriptions - Notification management interface for receiving alerts when subscribed task are modified.")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskFollowRest {

  @Resource
  private TaskFollowFacade taskFollowFacade;

  @Operation(summary = "Add the follow of task", operationId = "task:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Follow successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFollowFacade.add(id));
  }

  @Operation(summary = "Cancel the follow of task", operationId = "task:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFollowFacade.cancel(id);
  }

  @Operation(summary = "Cancel all the follows of task", operationId = "task:follow:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    taskFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query the follow list of task", operationId = "task:follow:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<TaskFollowDetailVo>> search(
      @Valid @ParameterObject TaskFollowFindDto dto) {
    return ApiLocaleResult.success(taskFollowFacade.search(dto));
  }

  @Operation(summary = "Query the follow number of task", operationId = "task:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(taskFollowFacade.count(projectId));
  }
}
