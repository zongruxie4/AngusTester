package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskRemarkFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.remark.TaskRemarkVo;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "TaskRemark")
@Validated
@RestController
@RequestMapping("/api/v1/task/remark")
public class TaskRemarkRest {

  @Resource
  private TaskRemarkFacade taskRemarkFacade;

  @ApiOperation(value = "Add the remark of task", nickname = "task:remark:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskRemarkAddDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.add(dto));
  }

  @ApiOperation(value = "Delete the remark of task", nickname = "task:remark:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Remark id", required = true) @PathVariable("id") Long id) {
    taskRemarkFacade.delete(id);
  }

  @ApiOperation(value = "Query the list of task remark", nickname = "task:remark:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskRemarkVo>> list(@Valid TaskRemarkFindDto dto) {
    return ApiLocaleResult.success(taskRemarkFacade.list(dto));
  }

}