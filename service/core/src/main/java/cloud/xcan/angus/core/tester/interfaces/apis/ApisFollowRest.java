package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.follow.ApisFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "API Follow", description = "API Follow Management - APIs for event management system integration, API resource change tracking, and monitoring system coordination")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisFollowRest {

  @Resource
  private ApisFollowFacade apisFollowFacade;

  @Operation(summary = "Follow API for monitoring",
      description = "Establish API follow relationship for event management system integration and change tracking",
      operationId = "apis:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API follow relationship established successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @Parameter(name = "apiId", description = "API identifier for follow relationship", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFollowFacade.add(apiId));
  }

  @Operation(summary = "Unfollow API monitoring",
      description = "Remove API follow relationship with proper cleanup and event system coordination",
      operationId = "apis:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API follow relationship removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/follow")
  public void cancel(
      @Parameter(name = "apiId", description = "API identifier for follow relationship removal", required = true) @PathVariable("apiId") Long apiId) {
    apisFollowFacade.cancel(apiId);
  }

  @Operation(summary = "Unfollow all API monitoring",
      description = "Remove all API follow relationships for specific project with comprehensive cleanup",
      operationId = "apis:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All API follow relationships removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for follow relationship cleanup") Long projectId) {
    apisFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query API follow list",
      description = "Retrieve paginated list of API follow relationships with comprehensive filtering and search options",
      operationId = "apis:follow:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API follow list retrieved successfully")})
  @GetMapping("/follow")
  public ApiLocaleResult<PageResult<ApisFollowDetailVo>> list(
      @Valid @ParameterObject ApisFollowFindDto dto) {
    return ApiLocaleResult.success(apisFollowFacade.list(dto));
  }

  @Operation(summary = "Get API follow count",
      description = "Retrieve total count of API follow relationships for specific project with comprehensive statistics",
      operationId = "apis:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API follow count retrieved successfully")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for follow count") Long projectId) {
    return ApiLocaleResult.success(apisFollowFacade.count(projectId));
  }
}
