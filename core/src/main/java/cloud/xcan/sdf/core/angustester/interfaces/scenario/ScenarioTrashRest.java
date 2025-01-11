package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.trash.ScenarioTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;
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

@Api(tags = "ScenarioTrash")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/trash")
public class ScenarioTrashRest {

  @Resource
  private ScenarioTrashFacade trashScenarioFacade;

  @ApiOperation(value = "Clear the trash of scenario or dir", nickname = "scenario:trash:clear")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    trashScenarioFacade.clear(id);
  }

  @ApiOperation(value = "Clear all the trash of scenario and dir ", nickname = "scenario:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    trashScenarioFacade.clearAll(projectId);
  }

  @ApiOperation(value = "Back the scenario or dir from the trash", nickname = "scenario:trash:back")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    trashScenarioFacade.back(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Back all the scenario and dir from trash", nickname = "scenario:trash:back:all")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    trashScenarioFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the number of all scenario and dir trash", nickname = "scenario:trash:count")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(trashScenarioFacade.count(projectId));
  }

  @ApiOperation(value = "Fulltext search the trash of scenario or dir", nickname = "scenario:trash:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScenarioTrashDetailVo>> search(
      @Valid ScenarioTrashSearchDto dto) {
    return ApiLocaleResult.success(trashScenarioFacade.search(dto));
  }

}