package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskMeetingFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Api(tags = "TaskMeeting")
@Validated
@RestController
@RequestMapping("/api/v1/task/meeting")
public class TaskMeetingRest {

  @Resource
  private TaskMeetingFacade taskMeetingFacade;

  @ApiOperation(value = "Add task meeting", nickname = "task:meeting:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskMeetingAddDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.add(dto));
  }

  @ApiOperation(value = "Update task meeting", nickname = "task:meeting:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskMeetingUpdateDto dto) {
    taskMeetingFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace task meeting", nickname = "task:meeting:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody TaskMeetingReplaceDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.replace(dto));
  }

  @ApiOperation(value = "Delete task meeting", nickname = "task:meeting:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Meeting id", required = true) @PathVariable("id") Long id) {
    taskMeetingFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of task meeting", nickname = "task:meeting:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskMeetingDetailVo> detail(
      @ApiParam(name = "id", value = "Meeting id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskMeetingFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of task meeting", nickname = "task:meeting:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskMeetingVo>> list(@Valid TaskMeetingFindDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of task meeting", nickname = "task:meeting:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskMeetingVo>> search(@Valid TaskMeetingSearchDto dto) {
    return ApiLocaleResult.success(taskMeetingFacade.search(dto));
  }

}
