package cloud.xcan.angus.core.tester.interfaces.issue;


import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.trash.TaskTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task Recycle Bin", description = "Task Recycle Bin Management API - Data recovery system for deleted tasks and sprints with restore capabilities, permanent deletion controls, and project-based organization")
@Validated
@RestController
@RequestMapping("/api/v1/task/trash")
public class TaskTrashRest {

  @Resource
  private TaskTrashFacade trashTaskFacade;

  @Operation(summary = "Permanently delete task or sprint from trash", operationId = "task:trash:clear", description = "Permanently remove a specific task or sprint from the recycle bin, making it unrecoverable")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task or sprint permanently deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash item identifier to permanently delete", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.clear(id);
  }

  @Operation(summary = "Clear all project trash items", operationId = "task:trash:clear:all", description = "Permanently remove all deleted tasks and sprints from a specific project's recycle bin")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All project trash items cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to clear all trash items from") Long projectId) {
    trashTaskFacade.clearAll(projectId);
  }

  @Operation(summary = "Restore task or sprint from trash", operationId = "task:trash:back", description = "Recover a deleted task or sprint from the recycle bin and restore it to its original location with all associated data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task or sprint restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash item identifier to restore", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restore all project trash items", operationId = "task:trash:back:all", description = "Recover all deleted tasks and sprints from a specific project's recycle bin to their original locations")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All project trash items restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to restore all trash items from") Long projectId) {
    trashTaskFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get project trash count", operationId = "task:trash:count", description = "Retrieve the total count of deleted tasks and sprints in a project's recycle bin for dashboard and cleanup planning")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project trash count retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier to count trash items for") Long projectId) {
    return ApiLocaleResult.success(trashTaskFacade.count(projectId));
  }

  @Operation(summary = "Get paginated trash items list", operationId = "task:trash:list", description = "Retrieve a paginated list of deleted tasks and sprints with filtering options for project, deletion date, and item type")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Trash items list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskTrashDetailVo>> list(
      @Valid @ParameterObject TaskTrashListDto dto) {
    return ApiLocaleResult.success(trashTaskFacade.list(dto));
  }

}
