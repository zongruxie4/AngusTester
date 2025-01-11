package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "TaskTrash")
@Validated
@RestController
@RequestMapping("/api/v1/task/trash")
public class TaskTrashRest {

  @Resource
  private TaskTrashFacade trashTaskFacade;

  @ApiOperation(value = "Clear the trash of task, sprint or project", nickname = "task:trash:clear")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.clear(id);
  }

  @ApiOperation(value = "Clear all the trash of task and sprint ", nickname = "task:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    trashTaskFacade.clearAll(projectId);
  }

  @ApiOperation(value = "Back the task, sprint or project from the trash", nickname = "task:trash:back")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    trashTaskFacade.back(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Back all the task and sprint from trash", nickname = "task:trash:back:all")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    trashTaskFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the number of all task and sprint trash", nickname = "task:trash:count")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(trashTaskFacade.count(projectId));
  }

  @ApiOperation(value = "Fulltext search the trash of task, sprint or project", nickname = "task:trash:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TaskTrashDetailVo>> search(@Valid TaskTrashSearchDto dto) {
    return ApiLocaleResult.success(trashTaskFacade.search(dto));
  }

}