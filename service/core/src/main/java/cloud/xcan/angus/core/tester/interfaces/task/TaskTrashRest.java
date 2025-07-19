package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.trash.TaskTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
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

@Tag(name = "TaskTrash", description = "Task Recycle Bin Management - Temporary storage for deleted task with restore capabilities and permanent deletion controls")
@Validated
@RestController
@RequestMapping("/api/v1/task/trash")
public class TaskTrashRest {

  @Resource
  private TaskTrashFacade trashTaskFacade;

  @Operation(summary = "Clear the trash of task or sprint", operationId = "task:trash:clear")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.clear(id);
  }

  @Operation(summary = "Clear all the trash of task and sprint ", operationId = "task:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    trashTaskFacade.clearAll(projectId);
  }

  @Operation(summary = "Back the task or sprint from the trash", operationId = "task:trash:back")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Back all the task and sprint from trash", operationId = "task:trash:back:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    trashTaskFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the count of all task and sprint trash", operationId = "task:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query count succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(trashTaskFacade.count(projectId));
  }

  @Operation(summary = "Query trash list of task or sprint", operationId = "task:trash:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskTrashDetailVo>> list(
      @Valid @ParameterObject TaskTrashListDto dto) {
    return ApiLocaleResult.success(trashTaskFacade.list(dto));
  }

}
