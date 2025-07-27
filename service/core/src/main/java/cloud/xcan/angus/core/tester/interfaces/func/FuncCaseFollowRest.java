package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow.FuncCaseFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
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

@Tag(name = "Functional Test Case Follow", description = "Functional Test Case Follow Management - Comprehensive APIs for subscribing to test case modifications and receiving notifications with change tracking capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFollowRest {

  @Resource
  private FuncCaseFollowFacade funcCaseFollowFacade;

  @Operation(summary = "Follow test case", 
      description = "Subscribe to a specific test case to receive notifications when modifications occur",
      operationId = "func:case:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test case follow subscription created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @Parameter(name = "caseId", description = "Test case identifier for follow subscription", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.add(caseId));
  }

  @Operation(summary = "Unfollow test case", 
      description = "Unsubscribe from a specific test case to stop receiving modification notifications",
      operationId = "func:case:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test case follow subscription removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/follow")
  public void cancel(
      @Parameter(name = "caseId", description = "Test case identifier for follow unsubscription", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFollowFacade.cancel(caseId);
  }

  @Operation(summary = "Unfollow all test cases", 
      description = "Unsubscribe from all test cases in a specific project to stop all follow notifications",
      operationId = "func:case:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All test case follow subscriptions removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for follow cleanup") Long projectId) {
    funcCaseFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "List followed test cases", 
      description = "Retrieve paginated list of followed test cases with comprehensive filtering and search options",
      operationId = "func:case:follow:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Followed test case list retrieved successfully")})
  @GetMapping("/follow")
  public ApiLocaleResult<PageResult<FuncCaseFollowDetailVo>> list(
      @Valid @ParameterObject FuncCaseFollowFindDto dto) {
    return ApiLocaleResult.success(funcCaseFollowFacade.list(dto));
  }

  @Operation(summary = "Get followed test case count", 
      description = "Retrieve the total count of followed test cases for a specific project",
      operationId = "func:case:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Followed test case count retrieved successfully")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for count retrieval") Long projectId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.count(projectId));
  }
}
