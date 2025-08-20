package cloud.xcan.angus.core.tester.interfaces.task;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskSprintFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
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

@Tag(name = "Task Sprint", description = "Task Sprint Management API - Iteration management system for Agile development cycles with progress tracking, timeline control, and team collaboration features")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintRest {

  @Resource
  private TaskSprintFacade taskSprintFacade;

  @Operation(summary = "Create new task sprint", operationId = "task:sprint:add", description = "Create a new sprint iteration with defined timeline, goals, and team assignments for organized task execution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task sprint created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskSprintAddDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.add(dto));
  }

  @Operation(summary = "Update task sprint details", operationId = "task:sprint:update", description = "Modify specific fields of an existing sprint while preserving other information and maintaining workflow integrity")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint updated successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskSprintUpdateDto dto) {
    taskSprintFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace task sprint completely", operationId = "task:sprint:replace", description = "Replace all sprint information with new data, effectively recreating the sprint with updated details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody TaskSprintReplaceDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.replace(dto));
  }

  @Operation(summary = "Start task sprint execution", operationId = "task:sprint:start", description = "Activate a sprint to begin task execution phase, enabling team members to start working on assigned tasks")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint started successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Sprint identifier to start execution", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "End task sprint execution", operationId = "task:sprint:end", description = "Complete a sprint iteration, finalizing all tasks and preparing for retrospective and planning phases")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint ended successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Sprint identifier to end execution", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Block task sprint", operationId = "task:sprint:block", description = "Temporarily pause sprint execution due to blockers or issues that prevent normal progress")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint blocked successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Sprint identifier to block", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone task sprint", operationId = "task:sprint:clone", description = "Create a duplicate of an existing sprint with all its configuration, tasks, and settings for reuse")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Sprint identifier to clone", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.clone(id));
  }

  @Operation(summary = "Move task sprint to different project", operationId = "task:sprint:move", description = "Transfer a sprint and all its associated tasks to another project for organizational restructuring")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint moved successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint or target project not found")
  })
  @PatchMapping("/{id}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @Parameter(name = "id", description = "Sprint identifier to move", required = true) @PathVariable("id") Long id,
      @Parameter(name = "targetProjectId", description = "Target project identifier for sprint transfer", required = true) @PathVariable("targetProjectId") Long targetProjectId) {
    taskSprintFacade.move(id, targetProjectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart tasks in sprint", operationId = "task:sprint:task:restart", description = "Reset multiple tasks within a sprint to their initial state for re-execution or testing purposes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint tasks restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Sprint or tasks not found")
  })
  @PatchMapping(value = "/task/restart")
  public ApiLocaleResult<?> restart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reopen tasks in sprint", operationId = "task:sprint:task:reopen", description = "Reopen completed or closed tasks within a sprint for additional work or corrections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint tasks reopened successfully"),
      @ApiResponse(responseCode = "404", description = "Sprint or tasks not found")
  })
  @PatchMapping(value = "/task/reopen")
  public ApiLocaleResult<?> reopen(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.reopen(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete task sprint", operationId = "task:sprint:delete", description = "Permanently remove a sprint and all its associated data including tasks, progress, and configuration")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task sprint deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Sprint identifier to delete", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.delete(id);
  }

  @Operation(summary = "Get task sprint details", operationId = "task:sprint:detail", description = "Retrieve details of a specific sprint including tasks, progress, timeline, and team assignments")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task sprint not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskSprintDetailVo> detail(
      @Parameter(name = "id", description = "Sprint identifier to retrieve details for", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.detail(id));
  }

  @Operation(summary = "Get paginated task sprints list", operationId = "task:sprint:list", description = "Retrieve a paginated list of sprints with filtering options for project, status, date range, and team criteria")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprints list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskSprintDetailVo>> list(
      @Valid @ParameterObject TaskSprintFindDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.list(dto));
  }

}
