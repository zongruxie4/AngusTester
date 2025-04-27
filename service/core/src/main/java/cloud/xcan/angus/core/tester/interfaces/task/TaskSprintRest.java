package cloud.xcan.angus.core.tester.interfaces.task;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskSprintFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
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
import jakarta.validation.constraints.Size;
import java.util.HashSet;
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

@Tag(name = "TaskSprint", description = "Task Sprint Management - Provides a dedicated entry point for managing the iterations of tasks, "
    + "enabling teams to track progress and make adjustments throughout the development cycle.")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintRest {

  @Resource
  private TaskSprintFacade taskSprintFacade;

  @Operation(description = "Add task sprint", operationId = "task:sprint:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskSprintAddDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.add(dto));
  }

  @Operation(description = "Update task sprint", operationId = "task:sprint:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskSprintUpdateDto dto) {
    taskSprintFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace task sprint", operationId = "task:sprint:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody TaskSprintReplaceDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.replace(dto));
  }

  @Operation(description = "Start task sprint", operationId = "task:sprint:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(description = "End task sprint", operationId = "task:sprint:end")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "End successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Block task sprint", operationId = "task:sprint:block")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Block successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Clone task sprint", operationId = "task:sprint:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.clone(id));
  }

  @Operation(description = "Move task sprint to another project", operationId = "task:sprint:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Moved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "targetProjectId", description = "Target project id", required = true) @PathVariable("targetProjectId") Long targetProjectId) {
    taskSprintFacade.move(id, targetProjectId);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Restart the task of sprint", operationId = "task:sprint:task:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/task/restart")
  public ApiLocaleResult<?> restart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Reopen the task of sprint", operationId = "task:sprint:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/task/reopen")
  public ApiLocaleResult<?> reopen(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.reopen(ids);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete task sprint", operationId = "task:sprint:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.delete(id);
  }

  @Operation(description = "Query the detail of task sprint", operationId = "task:sprint:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskSprintDetailVo> detail(
      @Parameter(name = "id", description = "Sprint id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.detail(id));
  }

  @Operation(description = "Query the list of task sprint", operationId = "task:sprint:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskSprintDetailVo>> list(@Valid TaskSprintFindDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.list(dto));
  }

  @Operation(description = "Fulltext search the list of task sprint", operationId = "task:sprint:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskSprintDetailVo>> search(@Valid TaskSprintSearchDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.search(dto));
  }

}
