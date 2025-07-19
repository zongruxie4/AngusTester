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

@Tag(name = "TaskMeeting", description = "Task Meeting Management - A unified management entry for all meetings within an Agile project")
@Validated
@RestController
@RequestMapping("/api/v1/task/meeting")
public class TaskMeetingRest {

  @Resource
  private TaskMeetingFacade taskMeetingFacade;

  @Operation(summary = "Add task meeting", operationId = "task:meeting:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskMeetingAddDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.add(dto));
  }

  @Operation(summary = "Update task meeting", operationId = "task:meeting:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskMeetingUpdateDto dto) {
    taskMeetingFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace task meeting", operationId = "task:meeting:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody TaskMeetingReplaceDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.replace(dto));
  }

  @Operation(summary = "Delete task meeting", operationId = "task:meeting:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Meeting id", required = true) @PathVariable("id") Long id) {
    taskMeetingFacade.delete(id);
  }

  @Operation(summary = "Query the detail of task meeting", operationId = "task:meeting:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskMeetingDetailVo> detail(
      @Parameter(name = "id", description = "Meeting id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskMeetingFacade.detail(id));
  }

  @Operation(summary = "Query the list of task meeting", operationId = "task:meeting:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskMeetingVo>> list(
      @Valid @ParameterObject TaskMeetingFindDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.list(dto));
  }

}
