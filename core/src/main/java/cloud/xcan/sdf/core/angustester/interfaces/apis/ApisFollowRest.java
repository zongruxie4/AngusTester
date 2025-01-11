package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.follow.ApisFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ApisFollow")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisFollowRest {

  @Resource
  private ApisFollowFacade apisFollowFacade;

  @ApiOperation(value = "Add the follow of apis", nickname = "apis:follow:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Follow successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @ApiParam(name = "apiId", value = "Apis id", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFollowFacade.add(apiId));
  }

  @ApiOperation(value = "Cancel the follow of apis", nickname = "apis:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/follow")
  public void cancel(
      @ApiParam(name = "apiId", value = "Apis id", required = true) @PathVariable("apiId") Long apiId) {
    apisFollowFacade.cancel(apiId);
  }

  @ApiOperation(value = "Cancel all the follows of apis", nickname = "apis:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    apisFollowFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Fulltext search apis follow", nickname = "apis:follow:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<ApisFollowDetailVo>> search(@Valid ApisFollowSearchDto dto) {
    return ApiLocaleResult.success(apisFollowFacade.search(dto));
  }

  @ApiOperation(value = "Query the follow number of apis", nickname = "apis:follow:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisFollowFacade.count(projectId));
  }
}