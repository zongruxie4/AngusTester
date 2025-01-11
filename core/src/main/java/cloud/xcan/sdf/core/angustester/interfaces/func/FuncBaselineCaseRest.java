package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncBaselineCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "FuncBaselineCase")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineCaseRest {

  @Resource
  private FuncBaselineCaseFacade funcBaselineCaseFacade;

  @ApiOperation(value = "Add baseline test cases", nickname = "func:baseline:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{baselineId}/case")
  public ApiLocaleResult<?> add(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.add(baselineId, caseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete baseline test cases", nickname = "func:baseline:case:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{baselineId}/case")
  public void delete(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.delete(baselineId, caseIds);
  }

  @ApiOperation(value = "Query the detail of baseline test cases", nickname = "func:baseline:case:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{baselineId}/case/{caseId}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @ApiParam(name = "caseId", value = "Case ID", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.detail(baselineId, caseId));
  }

  @ApiOperation(value = "Query the list of baseline test cases", nickname = "func:baseline:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{baselineId}/case")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.list(baselineId, dto));
  }

  @ApiOperation(value = "Fulltext search the list of baseline test cases", nickname = "func:baseline:case:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{baselineId}/case/search")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> search(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid FuncCaseSearchDto dto) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.search(baselineId, false, dto));
  }

  @ApiOperation(value = "Export the baseline test cases by conditions", nickname = "func:baseline:case:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/{baselineId}/case/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @ApiParam(name = "baselineId", value = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid FuncCaseSearchDto dto, HttpServletResponse response) {
    return funcBaselineCaseFacade.export(baselineId, dto, response);
  }

}
