package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.favourite.ApisFavouriteSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
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

@Api(tags = "ApisFavourite")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisFavouriteRest {

  @Resource
  private ApisFavouriteFacade apisFavouriteFacade;

  @ApiOperation(value = "Add the favourite of apis", nickname = "apis:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Favourite successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @ApiParam(name = "apiId", value = "apis id", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFavouriteFacade.add(apiId));
  }

  @ApiOperation(value = "Cancel the favourite of apis", nickname = "apis:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Canceled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/favourite")
  public void cancel(
      @ApiParam(name = "apiId", value = "apis id", required = true) @PathVariable("apiId") Long apiId) {
    apisFavouriteFacade.cancel(apiId);
  }

  @ApiOperation(value = "Cancel all the favourite of apis", nickname = "apis:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    apisFavouriteFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Fulltext search apis favourite", nickname = "apis:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<ApisFavouriteDetailVo>> search(ApisFavouriteSearchDto dto) {
    return ApiLocaleResult.success(apisFavouriteFacade.search(dto));
  }

  @ApiOperation(value = "Query the favourite number of apis", nickname = "apis:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisFavouriteFacade.count(projectId));
  }
}