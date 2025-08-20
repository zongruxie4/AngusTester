package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskMeetingFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task Meeting", description = "Task Meeting Management API - Meeting management system for Agile project collaboration including scheduling, updates, and participant coordination")
@Validated
@RestController
@RequestMapping("/api/v1/task/meeting")
public class TaskMeetingRest {

  @Resource
  private TaskMeetingFacade taskMeetingFacade;

  @Operation(summary = "Create new task meeting", operationId = "task:meeting:add", description = "Create a new meeting associated with a task, including scheduling, participant assignment, and agenda details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task meeting created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskMeetingAddDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.add(dto));
  }

  @Operation(summary = "Update task meeting details", operationId = "task:meeting:update", description = "Update specific fields of an existing task meeting while preserving other information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task meeting updated successfully"),
      @ApiResponse(responseCode = "404", description = "Task meeting not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskMeetingUpdateDto dto) {
    taskMeetingFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace task meeting completely", operationId = "task:meeting:replace", description = "Replace all meeting information with new data, effectively recreating the meeting with updated details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task meeting replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Task meeting not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody TaskMeetingReplaceDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.replace(dto));
  }

  @Operation(summary = "Delete task meeting", operationId = "task:meeting:delete", description = "Permanently remove a task meeting and all associated data including participant assignments and meeting records")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task meeting deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Meeting identifier to delete", required = true) @PathVariable("id") Long id) {
    taskMeetingFacade.delete(id);
  }

  @Operation(summary = "Get task meeting details", operationId = "task:meeting:detail", description = "Retrieve details of a specific task meeting including participants, agenda, and meeting history")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task meeting details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task meeting not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskMeetingDetailVo> detail(
      @Parameter(name = "id", description = "Meeting identifier to retrieve details for", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskMeetingFacade.detail(id));
  }

  @Operation(summary = "Get paginated task meetings list", operationId = "task:meeting:list", description = "Retrieve a paginated list of task meetings with filtering options for project, date range, and participant criteria")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task meetings list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskMeetingVo>> list(
      @Valid @ParameterObject TaskMeetingFindDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.list(dto));
  }

}
