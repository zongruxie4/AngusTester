package cloud.xcan.sdf.core.angustester.interfaces.task;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskSprintFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Size;
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

@Api(tags = "TaskSprint")
@Validated
@RestController
@RequestMapping("/api/v1/task/sprint")
public class TaskSprintRest {

  @Resource
  private TaskSprintFacade taskSprintFacade;

  @ApiOperation(value = "Add task sprint", nickname = "task:sprint:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TaskSprintAddDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.add(dto));
  }

  @ApiOperation(value = "Update task sprint", nickname = "task:sprint:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TaskSprintUpdateDto dto) {
    taskSprintFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace task sprint", nickname = "task:sprint:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody TaskSprintReplaceDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.replace(dto));
  }

  @ApiOperation(value = "Start task sprint", nickname = "task:sprint:start")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Started successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.start(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "End task sprint", nickname = "task:sprint:end")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "End successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.end(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Block task sprint", nickname = "task:sprint:block")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Block successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.block(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone task sprint", nickname = "task:sprint:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.clone(id));
  }

  @ApiOperation(value = "Move task sprint to another project", nickname = "task:sprint:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Moved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "targetProjectId", value = "Target project id", required = true) @PathVariable("targetProjectId") Long targetProjectId) {
    taskSprintFacade.move(id, targetProjectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the task of sprint", nickname = "task:sprint:task:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/task/restart")
  public ApiLocaleResult<?> restart(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reopen the task of sprint", nickname = "task:sprint:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/task/reopen")
  public ApiLocaleResult<?> reopen(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    taskSprintFacade.reopen(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete task sprint", nickname = "task:sprint:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    taskSprintFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of task sprint", nickname = "task:sprint:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TaskSprintDetailVo> detail(
      @ApiParam(name = "id", value = "Sprint id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(taskSprintFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of task sprint", nickname = "task:sprint:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<TaskSprintDetailVo>> list(@Valid TaskSprintFindDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of task sprint", nickname = "task:sprint:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskSprintDetailVo>> search(@Valid TaskSprintSearchDto dto) {
    return ApiLocaleResult.success(taskSprintFacade.search(dto));
  }

}
