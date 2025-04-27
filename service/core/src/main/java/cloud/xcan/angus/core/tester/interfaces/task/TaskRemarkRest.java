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

@Tag(name = "TaskRemark", description = "Task Remark Management - A unified management entry for all task remarks.")
@Validated
@RestController
@RequestMapping("/api/v1/task/remark")
public class TaskRemarkRest {

  @Resource
  private TaskRemarkFacade taskRemarkFacade;

  @Operation(description = "Add the remark of task", operationId = "task:remark:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskRemarkAddDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.add(dto));
  }

  @Operation(description = "Delete the remark of task", operationId = "task:remark:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Remark id", required = true) @PathVariable("id") Long id) {
    taskRemarkFacade.delete(id);
  }

  @Operation(description = "Query the list of task remark", operationId = "task:remark:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskRemarkVo>> list(@Valid TaskRemarkFindDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.list(dto));
  }

}
