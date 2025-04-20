package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
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

@Api(tags = "FuncCaseFollow")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFollowRest {

  @Resource
  private FuncCaseFollowFacade funcCaseFollowFacade;

  @Operation(description = "Add the follow of case", operationId = "func:case:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Follow successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @Parameter(name = "caseId", description = "Case id", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.add(caseId));
  }

  @Operation(description = "Cancel the follow of case", operationId = "func:case:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/follow")
  public void cancel(
      @Parameter(name = "caseId", description = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFollowFacade.cancel(caseId);
  }

  @Operation(description = "Cancel all the follows of case", operationId = "func:case:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    funcCaseFollowFacade.cancelAll(projectId);
  }

  @Operation(description = "Fulltext search case follow", operationId = "func:case:follow:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<FuncCaseFollowDetailVo>> search(
      @Valid FuncCaseFollowSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFollowFacade.search(dto));
  }

  @Operation(description = "Query the follow number of case", operationId = "func:case:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.count(projectId));
  }
}
