package cloud.xcan.sdf.core.angustester.interfaces.project;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.ProjectTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ProjectTrash")
@Validated
@RestController
@RequestMapping("/api/v1/project/trash")
public class ProjectTrashRest {

  @Resource
  private ProjectTrashFacade projectTrashFacade;

  @ApiOperation(value = "Clear the trash of project", nickname = "project:trash:clear")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    projectTrashFacade.clear(id);
  }

  @ApiOperation(value = "Clear all the trash of project ", nickname = "project:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping()
  public void clearAll() {
    projectTrashFacade.clearAll();
  }

  @ApiOperation(value = "Back the project from the trash", nickname = "project:trash:back")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    projectTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Back all the project from trash", nickname = "project:trash:back:all")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll() {
    projectTrashFacade.backAll();
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the number of all project trash", nickname = "project:trash:count")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count() {
    return ApiLocaleResult.success(projectTrashFacade.count());
  }

  @ApiOperation(value = "Fulltext search the trash of project", nickname = "project:trash:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ProjectTrashDetailVo>> search(
      @Valid ProjectTrashSearchDto dto) {
    return ApiLocaleResult.success(projectTrashFacade.search(dto));
  }

}