package cloud.xcan.angus.core.tester.interfaces.task;


import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskRemarkFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.remark.TaskRemarkVo;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task Remark", description = "Task Remark Management API - Comprehensive comment and annotation system for task collaboration including threaded discussions and contextual notes.")
@Validated
@RestController
@RequestMapping("/api/v1/task/remark")
public class TaskRemarkRest {

  @Resource
  private TaskRemarkFacade taskRemarkFacade;

  @Operation(summary = "Add task remark/comment", operationId = "task:remark:add", description = "Create a new remark or comment on a task for collaboration, feedback, or contextual information sharing.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task remark created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskRemarkAddDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.add(dto));
  }

  @Operation(summary = "Delete task remark", operationId = "task:remark:delete", description = "Permanently remove a specific remark or comment from a task, including all associated metadata.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task remark deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Remark identifier to delete", required = true) @PathVariable("id") Long id) {
    taskRemarkFacade.delete(id);
  }

  @Operation(summary = "Get paginated task remarks list", operationId = "task:remark:list", description = "Retrieve a paginated list of task remarks with filtering options for task, author, and date range criteria.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task remarks list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskRemarkVo>> list(
      @Valid @ParameterObject TaskRemarkFindDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.list(dto));
  }

}
