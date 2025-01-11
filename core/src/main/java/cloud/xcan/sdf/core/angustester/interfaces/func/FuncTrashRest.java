package cloud.xcan.sdf.core.angustester.interfaces.func;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.trash.FuncTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.trash.FuncTrashDetailVo;
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

@Api(tags = "FuncTrash")
@Validated
@RestController
@RequestMapping("/api/v1/func/trash")
public class FuncTrashRest {

  @Resource
  private FuncTrashFacade funcTrashFacade;

  @ApiOperation(value = "Clear the trash of functional test", nickname = "func:trash:clear")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.clear(id);
  }

  @ApiOperation(value = "Clear all the trash of functional test", nickname = "func:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    funcTrashFacade.clearAll(projectId);
  }

  @ApiOperation(value = "Back the functional test from the trash", nickname = "func:trash:back")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @ApiParam(name = "id", value = "Trash id", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Back all the functional test from the trash", nickname = "func:trash:back:all")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    funcTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the number of functional test", nickname = "func:trash:count")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcTrashFacade.count(projectId));
  }

  @ApiOperation(value = "Fulltext search the trash of functional test", nickname = "func:trash:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncTrashDetailVo>> search(@Valid FuncTrashSearchDto dto) {
    return ApiLocaleResult.success(funcTrashFacade.search(dto));
  }

}
