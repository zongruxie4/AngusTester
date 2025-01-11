package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Api(tags = "TaskFollow")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskFollowRest {

  @Resource
  private TaskFollowFacade taskFollowFacade;

  @ApiOperation(value = "Add the follow of task", nickname = "task:follow:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Follow successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFollowFacade.add(id));
  }

  @ApiOperation(value = "Cancel the follow of task", nickname = "task:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFollowFacade.cancel(id);
  }

  @ApiOperation(value = "Cancel all the follows of task", nickname = "task:follow:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    taskFollowFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Query the follow list of task", nickname = "task:follow:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<TaskFollowDetailVo>> search(
      @Valid TaskFollowFindDto dto) {
    return ApiLocaleResult.success(taskFollowFacade.search(dto));
  }

  @ApiOperation(value = "Query the follow number of task", nickname = "task:follow:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(taskFollowFacade.count(projectId));
  }
}
