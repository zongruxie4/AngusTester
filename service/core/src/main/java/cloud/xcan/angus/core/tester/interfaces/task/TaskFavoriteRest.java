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

@Tag(name = "Task- Favourite", description = "Task Favorites Management API - Bookmarking system for quick access to frequently used tasks with project-based organization")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskFavoriteRest {

  @Resource
  private TaskFavouriteFacade taskFavouriteFacade;

  @Operation(summary = "Add task to favorites", operationId = "task:favourite:add", description = "Add a specific task to the user's favorites list for quick access and priority visibility")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task added to favorites successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Task identifier to add to favorites", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFavouriteFacade.add(id));
  }

  @Operation(summary = "Remove task from favorites", operationId = "task:favourite:delete", description = "Remove a specific task from the user's favorites list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task removed from favorites successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/favourite")
  public void cancel(
      @Parameter(name = "id", description = "Task identifier to remove from favorites", required = true) @PathVariable("id") Long id) {
    taskFavouriteFacade.cancel(id);
  }

  @Operation(summary = "Clear all task favorites in project", operationId = "task:favourite:delete:all", description = "Remove all favourited tasks within a specific project from the user's favorites list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All project favorites cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to clear all favorites from") Long projectId) {
    taskFavouriteFacade.cancelAll(projectId);
  }

  @Operation(summary = "Get paginated favorites list", operationId = "task:favourite:list", description = "Retrieve a paginated list of favourited tasks with detailed information including task metadata and favorite timestamps")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Favorites list retrieved successfully")})
  @GetMapping("/favourite")
  public ApiLocaleResult<PageResult<TaskFavouriteDetailVo>> list(
      @Valid @ParameterObject TaskFavouriteFindDto dto) {
    return ApiLocaleResult.success(taskFavouriteFacade.list(dto));
  }

  @Operation(summary = "Get favorites count by project", operationId = "task:favourite:count", description = "Retrieve the total count of favourited tasks within a specific project for statistical purposes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Favorites count retrieved successfully")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to count favorites for") Long projectId) {
    return ApiLocaleResult.success(taskFavouriteFacade.count(projectId));
  }

}
