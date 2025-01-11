package cloud.xcan.sdf.core.angustester.interfaces.task;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAssigneeReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAttachmentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskConfirmorReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskDescriptionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskMoveDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskTagReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskVersionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
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

@Api(tags = "Task")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskRest {

  @Resource
  private TaskFacade taskFacade;

  @ApiOperation(value = "Create task", nickname = "task:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskAddDto dto) {
    return ApiLocaleResult.success(taskFacade.add(dto));
  }

  @ApiOperation(value = "Update task", nickname = "task:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}")
  public ApiLocaleResult<?> update(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskUpdateDto dto) {
    taskFacade.update(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace task", nickname = "task:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskReplaceDto dto) {
    taskFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the name of task", nickname = "task:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "name", value = "New task name", required = true) @Valid @Length(max = DEFAULT_NAME_LENGTH_X4) @RequestParam("name") String name) {
    taskFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Move the task to another sprint", nickname = "task:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Move successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/move")
  public ApiLocaleResult<?> move(@Valid @RequestBody TaskMoveDto dto) {
    taskFacade.move(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the type of task", nickname = "task:type:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/type")
  public ApiLocaleResult<?> replaceType(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "type", value = "Task type", required = true) @RequestParam("type") String type) {
    taskFacade.replaceType(id, type);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the bug level of task", nickname = "task:bugLevel:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/bugLevel")
  public ApiLocaleResult<?> replaceBugLevel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "bugLevel", value = "Bug level", required = true) @RequestParam("bugLevel") String bugLevel) {
    taskFacade.replaceBugLevel(id, bugLevel);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the bug level of task", nickname = "task:missingBugFlag:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/missingBugFlag")
  public ApiLocaleResult<?> replaceMissingBugFlag(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "missingBugFlag", value = "Missing bug flag", required = true) @RequestParam("missingBugFlag") Boolean missingBugFlag) {
    taskFacade.replaceMissingBugFlag(id, missingBugFlag);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the assignees of task", nickname = "task:assignee:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/assignee")
  public ApiLocaleResult<?> replaceAssignee(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskAssigneeReplaceDto dto) {
    taskFacade.replaceAssignee(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the confirmor of task", nickname = "task:confirmor:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/confirmor")
  public ApiLocaleResult<?> replaceConfirmor(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskConfirmorReplaceDto dto) {
    taskFacade.replaceConfirmor(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the tags of task", nickname = "task:tags:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tag")
  public ApiLocaleResult<?> replaceTag(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskTagReplaceDto dto) {
    taskFacade.replaceTag(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the deadline of task", nickname = "task:deadline:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/deadline/{deadline}")
  public ApiLocaleResult<?> replaceDeadline(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "deadline", value = "Task deadline", required = true) @PathVariable("deadline") @DateTimeFormat(pattern = DATE_FMT) LocalDateTime deadlineDate) {
    taskFacade.replaceDeadline(id, deadlineDate);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the priority of task", nickname = "task:priority:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/priority/{priority}")
  public ApiLocaleResult<?> replacePriority(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "priority", value = "Task priority", required = true) @PathVariable("priority") Priority priority) {
    taskFacade.replacePriority(id, priority);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the release version of task", nickname = "task:version:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/version")
  public ApiLocaleResult<?> replaceVersion(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskVersionReplaceDto version) {
    taskFacade.replaceVersion(id, version);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the eval workload of task", nickname = "task:evalWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/evalWorkload")
  public ApiLocaleResult<?> replaceEvalWorkload(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskWorkloadReplaceDto dto) {
    taskFacade.replaceEvalWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the actual workload of task", nickname = "task:actualWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/actualWorkload")
  public ApiLocaleResult<?> replaceActualWorkload(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskWorkloadReplaceDto dto) {
    taskFacade.replaceActualWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the description of task", nickname = "task:description:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/description")
  public ApiLocaleResult<?> replaceDescription(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskDescriptionReplaceDto dto) {
    taskFacade.replaceDescription(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace attachment the task", nickname = "task:attachment:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/attachment")
  public ApiLocaleResult<?> replaceAttachment(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody TaskAttachmentReplaceDto dto) {
    taskFacade.replaceAttachment(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Start task", nickname = "task:start")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Started successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.start(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel task", nickname = "task:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Canceled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/cancel")
  public ApiLocaleResult<?> cancel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.cancel(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Modify task as processed", nickname = "task:processed")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Modify successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/processed")
  public ApiLocaleResult<?> processed(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.processed(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Confirm the process result of task", nickname = "task:process:result:confirm")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Confirmed successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/result/{result}/confirm")
  public ApiLocaleResult<?> confirmResult(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "result", value = "Task process result", required = true) @PathVariable("result") Result result,
      @ApiParam(name = "evalWorkload", value = "Task eval workload") @RequestParam(value = "evalWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal evalWorkload,
      @ApiParam(name = "actualWorkload", value = "Task actual workload") @RequestParam(value = "actualWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal actualWorkload) {
    taskFacade.confirm(id, result, evalWorkload, actualWorkload);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Complete the task", nickname = "task:complete")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Completed successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/complete")
  public ApiLocaleResult<?> complete(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "evalWorkload", value = "Task eval workload") @RequestParam(value = "evalWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal evalWorkload,
      @ApiParam(name = "actualWorkload", value = "Task actual workload") @RequestParam(value = "actualWorkload", required = false)
      @Valid @Min(0) @Max(MAX_WORKLOAD_NUM) BigDecimal actualWorkload) {
    taskFacade.complete(id, evalWorkload, actualWorkload);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Modify the subtasks of the task", nickname = "task:subtask:set")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Set successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/subtask/set")
  public ApiLocaleResult<?> subtaskSet(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("subTaskIds") HashSet<Long> subTaskIds) {
    taskFacade.subtaskSet(id, subTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel the subtasks of the task", nickname = "task:subtask:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cancel successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/subtask/cancel")
  public ApiLocaleResult<?> subtaskCancel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("subTaskIds") HashSet<Long> subTaskIds) {
    taskFacade.subtaskCancel(id, subTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the not associated subtask list of task", nickname = "task:notAssociated:subtask:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/subtask/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedSubtask(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(taskFacade.notAssociatedSubtask(id, moduleId));
  }

  @ApiOperation(value = "Associate the tasks of the task", nickname = "task:association:task:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Set successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/association/task")
  public ApiLocaleResult<?> taskAssocAdd(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    taskFacade.taskAssocAdd(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel the association tasks of the task", nickname = "task:association:task:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cancel successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/task/cancel")
  public ApiLocaleResult<?> taskAssocCancel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    taskFacade.taskAssocCancel(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the not associated tasks list of task", nickname = "task:notAssociated:task:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/task/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedTask(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @ApiParam(name = "taskType", value = "Task type", required = false) @RequestParam(value = "taskType", required = false) TaskType taskType) {
    return ApiLocaleResult.success(taskFacade.notAssociatedTask(id, moduleId, taskType));
  }

  @ApiOperation(value = "Associate the cases of the task", nickname = "task:association:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Set successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/association/case")
  public ApiLocaleResult<?> caseAssocAdd(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    taskFacade.caseAssocAdd(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel the association cases of the task", nickname = "task:association:case:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cancel successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/case/cancel")
  public ApiLocaleResult<?> caseAssocCancel(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    taskFacade.caseAssocCancel(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the not associated cases list of task", nickname = "task:notAssociated:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/case/notAssociated")
  public ApiLocaleResult<List<FuncCaseListVo>> notAssociatedCase(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(taskFacade.notAssociatedCase(id, moduleId));
  }

  @ApiOperation(value = "Delete tasks", nickname = "task:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Task ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskFacade.delete(ids);
  }

  @ApiOperation(value = "Restart task", nickname = "task:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful retest", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/{id}/restart")
  public ApiLocaleResult<?> restart(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.restart(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reopen task", nickname = "task:reopen")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful reopen", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/{id}/reopen")
  public ApiLocaleResult<?> reopen(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    taskFacade.reopen(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the detail of task", nickname = "task:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskDetailVo> detail(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of task", nickname = "task:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskListVo>> list(@Valid TaskFindDto dto) {
    return ApiLocaleResult.success(taskFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of task", nickname = "task:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskListVo>> search(@Valid TaskSearchDto dto) {
    return ApiLocaleResult.success(taskFacade.search(false, dto));
  }

  @ApiOperation(value = "Import the tasks", nickname = "task:import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(@Valid TaskImportDto dto) {
    return ApiLocaleResult.success(taskFacade.imports(dto));
  }

  @ApiOperation(value = "Export the tasks by conditions", nickname = "task:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(@Valid TaskSearchDto dto,
      HttpServletResponse response) {
    return taskFacade.export(dto, response);
  }
}
