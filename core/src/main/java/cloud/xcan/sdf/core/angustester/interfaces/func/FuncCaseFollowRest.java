package cloud.xcan.sdf.core.angustester.interfaces.func;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
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

@Api(tags = "FuncCaseFollow")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFollowRest {

  @Resource
  private FuncCaseFollowFacade funcCaseFollowFacade;

  @ApiOperation(value = "Add the follow of case", nickname = "func:case:follow:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Follow successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @ApiParam(name = "caseId", value = "Case id", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.add(caseId));
  }

  @ApiOperation(value = "Cancel the follow of case", nickname = "func:case:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/follow")
  public void cancel(
      @ApiParam(name = "caseId", value = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFollowFacade.cancel(caseId);
  }

  @ApiOperation(value = "Cancel all the follows of case", nickname = "func:case:follow:cancel:All")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    funcCaseFollowFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Fulltext search case follow", nickname = "func:case:follow:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<FuncCaseFollowDetailVo>> search(
      @Valid FuncCaseFollowSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFollowFacade.search(dto));
  }

  @ApiOperation(value = "Query the follow number of case", nickname = "func:case:follow:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcCaseFollowFacade.count(projectId));
  }
}