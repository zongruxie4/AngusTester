package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.trash.ApisTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;
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

@Api(tags = "ApisTrash")
@Validated
@RestController
@RequestMapping("/api/v1/apis/trash")
public class ApisTrashRest {

  @Resource
  private ApisTrashFacade apisTrashFacade;

  @ApiOperation(value = "Clear the trash of services or apis", nickname = "apis:trash:clear")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.clear(id);
  }

  @ApiOperation(value = "Clear all the trash of services and api apis", nickname = "apis:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping()
  public void clearAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    apisTrashFacade.clearAll(projectId);
  }

  @ApiOperation(value = "Back the services or apis from the trash", nickname = "apis:trash:back")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Back all the services and apis from the trash", nickname = "apis:trash:back:all")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    apisTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the number of all services and api trash", nickname = "apis:trash:count")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisTrashFacade.count(projectId));
  }

  @ApiOperation(value = "Fulltext search the trash of services or apis", nickname = "apis:trash:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisTrashDetailVo>> search(@Valid ApisTrashSearchDto dto) {
    return ApiLocaleResult.success(apisTrashFacade.search(dto));
  }

}