package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.favorite.TaskFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.favorite.TaskFavouriteDetailVo;
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

@Tag(name = "TaskFavourite", description = "Task Favorites Management - Quick-access mechanism for bookmarking frequently used task")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskFavoriteRest {

  @Resource
  private TaskFavouriteFacade taskFavouriteFacade;

  @Operation(summary = "Add the favourite of task", operationId = "task:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFavouriteFacade.add(id));
  }

  @Operation(summary = "Cancel the favourite of task", operationId = "task:favourite:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/favourite")
  public void cancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFavouriteFacade.cancel(id);
  }

  @Operation(summary = "Cancel all the favorites of task", operationId = "task:favourite:delete:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    taskFavouriteFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query the favourite list of task", operationId = "task:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<TaskFavouriteDetailVo>> search(
      @Valid @ParameterObject TaskFavouriteFindDto dto) {
    return ApiLocaleResult.success(taskFavouriteFacade.search(dto));
  }

  @Operation(summary = "Query the favourite number of task", operationId = "task:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(taskFavouriteFacade.count(projectId));
  }

}
