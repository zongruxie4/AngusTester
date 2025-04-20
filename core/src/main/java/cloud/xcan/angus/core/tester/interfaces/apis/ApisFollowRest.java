package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.follow.ApisFollowSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

  @Operation(description = "Add the follow of apis", operationId = "apis:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Follow successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @Parameter(name = "apiId", description = "Apis id", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFollowFacade.add(apiId));
  }

  @Operation(description = "Cancel the follow of apis", operationId = "apis:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/follow")
  public void cancel(
      @Parameter(name = "apiId", description = "Apis id", required = true) @PathVariable("apiId") Long apiId) {
    apisFollowFacade.cancel(apiId);
  }

  @Operation(description = "Cancel all the follows of apis", operationId = "apis:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    apisFollowFacade.cancelAll(projectId);
  }

  @Operation(description = "Fulltext search apis follow", operationId = "apis:follow:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<ApisFollowDetailVo>> search(@Valid ApisFollowSearchDto dto) {
    return ApiLocaleResult.success(apisFollowFacade.search(dto));
  }

  @Operation(description = "Query the follow number of apis", operationId = "apis:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisFollowFacade.count(projectId));
  }
}
