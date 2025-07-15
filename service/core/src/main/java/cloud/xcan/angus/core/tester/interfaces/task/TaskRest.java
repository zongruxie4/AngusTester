package cloud.xcan.angus.core.tester.interfaces.task;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAssigneeReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAttachmentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskConfirmorReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskDescriptionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskImportDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskMoveDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskTagReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskWorkloadReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Task", description = "R&D and Testing Task Management - A unified management entry for all tasks related to R&D and testing")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskRest {

  @Resource
  private TaskFacade taskFacade;

  @Operation(summary = "Create task", operationId = "task:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskAddDto dto) {
    return ApiLocaleResult.success(taskFacade.add(dto));
  }

  @Operation(summary = "Update task", operationId = "task:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}")
  public ApiLocaleResult<?> update(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskUpdateDto dto) {
    taskFacade.update(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace task", operationId = "task:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskReplaceDto dto) {
    taskFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the name of task", operationId = "task:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New task name", required = true) @RequestParam("name") String name) {
    taskFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Move the task to another sprint", operationId = "task:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Move successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/move")
  public ApiLocaleResult<?> move(@Valid @RequestBody TaskMoveDto dto) {
    taskFacade.move(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the type of task", operationId = "task:type:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/type")
  public ApiLocaleResult<?> replaceType(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "type", description = "Task type", required = true) @RequestParam("type") String type) {
    taskFacade.replaceType(id, type);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the bug level of task", operationId = "task:bugLevel:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/bugLevel")
  public ApiLocaleResult<?> replaceBugLevel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "bugLevel", description = "Bug level", required = true) @RequestParam("bugLevel") String bugLevel) {
    taskFacade.replaceBugLevel(id, bugLevel);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the bug level of task", operationId = "task:missingBug:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/missingBug")
  public ApiLocaleResult<?> replaceMissingBug(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "missingBug", description = "Missing bug flag", required = true) @RequestParam("missingBug") Boolean missingBug) {
    taskFacade.replaceMissingBug(id, missingBug);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the assignees of task", operationId = "task:assignee:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/assignee")
  public ApiLocaleResult<?> replaceAssignee(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskAssigneeReplaceDto dto) {
    taskFacade.replaceAssignee(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the confirmor of task", operationId = "task:confirmor:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/confirmor")
  public ApiLocaleResult<?> replaceConfirmor(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskConfirmorReplaceDto dto) {
    taskFacade.replaceConfirmor(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the tags of task", operationId = "task:tags:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tag")
  public ApiLocaleResult<?> replaceTag(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskTagReplaceDto dto) {
    taskFacade.replaceTag(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the deadline of task", operationId = "task:deadline:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/deadline/{deadline}")
  public ApiLocaleResult<?> replaceDeadline(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "deadline", description = "Task deadline", required = true) @PathVariable("deadline") @DateTimeFormat(pattern = DATE_FMT) LocalDateTime deadlineDate) {
    taskFacade.replaceDeadline(id, deadlineDate);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the priority of task", operationId = "task:priority:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/priority/{priority}")
  public ApiLocaleResult<?> replacePriority(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "priority", description = "Task priority", required = true) @PathVariable("priority") Priority priority) {
    taskFacade.replacePriority(id, priority);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the software version of task", operationId = "task:softwareVersion:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/software/version")
  public ApiLocaleResult<?> replaceSoftwareVersion(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody SoftwareVersionRefReplaceDto version) {
    taskFacade.replaceSoftwareVersion(id, version);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the eval workload of task", operationId = "task:evalWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/evalWorkload")
  public ApiLocaleResult<?> replaceEvalWorkload(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskWorkloadReplaceDto dto) {
    taskFacade.replaceEvalWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the actual workload of task", operationId = "task:actualWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/actualWorkload")
  public ApiLocaleResult<?> replaceActualWorkload(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskWorkloadReplaceDto dto) {
    taskFacade.replaceActualWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the description of task", operationId = "task:description:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/description")
  public ApiLocaleResult<?> replaceDescription(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskDescriptionReplaceDto dto) {
    taskFacade.replaceDescription(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace attachment the task", operationId = "task:attachment:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/attachment")
  public ApiLocaleResult<?> replaceAttachment(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskAttachmentReplaceDto dto) {
    taskFacade.replaceAttachment(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Start task", operationId = "task:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel task", operationId = "task:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Canceled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/cancel")
  public ApiLocaleResult<?> cancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.cancel(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Modify task as processed", operationId = "task:processed")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modify successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/processed")
  public ApiLocaleResult<?> processed(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.processed(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Confirm the process result of task", operationId = "task:process:result:confirm")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Confirmed successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/result/{result}/confirm")
  public ApiLocaleResult<?> confirmResult(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "result", description = "Task process result", required = true) @PathVariable("result") Result result,
      @Parameter(name = "evalWorkload", description = "Task eval workload") @RequestParam(value = "evalWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal evalWorkload,
      @Parameter(name = "actualWorkload", description = "Task actual workload") @RequestParam(value = "actualWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal actualWorkload) {
    taskFacade.confirm(id, result, evalWorkload, actualWorkload);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Complete the task", operationId = "task:complete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Completed successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/complete")
  public ApiLocaleResult<?> complete(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "evalWorkload", description = "Task eval workload") @RequestParam(value = "evalWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal evalWorkload,
      @Parameter(name = "actualWorkload", description = "Task actual workload") @RequestParam(value = "actualWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal actualWorkload) {
    taskFacade.complete(id, evalWorkload, actualWorkload);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Modify the subtasks of the task", operationId = "task:subtask:set")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Set successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/subtask/set")
  public ApiLocaleResult<?> subtaskSet(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("subTaskIds") HashSet<Long> subTaskIds) {
    taskFacade.subtaskSet(id, subTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel the subtasks of the task", operationId = "task:subtask:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cancel successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/subtask/cancel")
  public ApiLocaleResult<?> subtaskCancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("subTaskIds") HashSet<Long> subTaskIds) {
    taskFacade.subtaskCancel(id, subTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the not associated subtask list of task", operationId = "task:notAssociated:subtask:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/subtask/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedSubtask(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(taskFacade.notAssociatedSubtask(id, moduleId));
  }

  @Operation(summary = "Associate the tasks of the task", operationId = "task:association:task:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Set successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/association/task")
  public ApiLocaleResult<?> taskAssocAdd(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    taskFacade.taskAssocAdd(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel the association tasks of the task", operationId = "task:association:task:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cancel successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/task/cancel")
  public ApiLocaleResult<?> taskAssocCancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    taskFacade.taskAssocCancel(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the not associated tasks list of task", operationId = "task:notAssociated:task:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/task/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedTask(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "taskType", description = "Task type", required = false) @RequestParam(value = "taskType", required = false) TaskType taskType) {
    return ApiLocaleResult.success(taskFacade.notAssociatedTask(id, moduleId, taskType));
  }

  @Operation(summary = "Associate the cases of the task", operationId = "task:association:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Set successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/association/case")
  public ApiLocaleResult<?> caseAssocAdd(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    taskFacade.caseAssocAdd(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel the association cases of the task", operationId = "task:association:case:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cancel successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/case/cancel")
  public ApiLocaleResult<?> caseAssocCancel(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    taskFacade.caseAssocCancel(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the not associated cases list of task", operationId = "task:notAssociated:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/case/notAssociated")
  public ApiLocaleResult<List<FuncCaseListVo>> notAssociatedCase(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(taskFacade.notAssociatedCase(id, moduleId));
  }

  @Operation(summary = "Restart task", operationId = "task:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful retest"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/{id}/restart")
  public ApiLocaleResult<?> restart(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.restart(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reopen task", operationId = "task:reopen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful reopen"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping("/{id}/reopen")
  public ApiLocaleResult<?> reopen(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.reopen(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import the tasks", operationId = "task:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid TaskImportDto dto) {
    return ApiLocaleResult.success(taskFacade.imports(dto));
  }

  @Operation(summary = "Import the inner task example", operationId = "task:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(taskFacade.importExample(projectId));
  }

  @Operation(summary = "Delete tasks", operationId = "task:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Task ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of task", operationId = "task:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskDetailVo> detail(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFacade.detail(id));
  }

  @Operation(summary = "Query the list of task", operationId = "task:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskListVo>> list(@Valid @ParameterObject TaskFindDto dto) {
    return ApiLocaleResult.success(taskFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of task", operationId = "task:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskListVo>> search(@Valid @ParameterObject TaskSearchDto dto) {
    return ApiLocaleResult.success(taskFacade.search(false, dto));
  }

  @Operation(summary = "Export the tasks by conditions", operationId = "task:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject TaskSearchDto dto, HttpServletResponse response) {
    return taskFacade.export(dto, response);
  }

}
