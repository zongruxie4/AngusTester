package cloud.xcan.sdf.core.angustester.interfaces.func;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "FuncCaseFavourite")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFavouriteRest {

  @Resource
  private FuncCaseFavouriteFacade funcCaseFavouriteFacade;

  @ApiOperation(value = "Add the favourite of case", nickname = "func:case:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Favourite successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @ApiParam(name = "caseId", value = "Case id", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.add(caseId));
  }

  @ApiOperation(value = "Cancel the favourite of case", nickname = "func:case:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Canceled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/favourite")
  public void cancel(
      @ApiParam(name = "caseId", value = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFavouriteFacade.cancel(caseId);
  }

  @ApiOperation(value = "Cancel all the favourite of case", nickname = "func:case:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    funcCaseFavouriteFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Fulltext search case favourite", nickname = "func:case:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<FuncCaseFavouriteDetailVo>> search(
      FuncCaseFavouriteSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.search(dto));
  }

  @ApiOperation(value = "Query the favourite number of case", nickname = "func:case:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.count(projectId));
  }
}