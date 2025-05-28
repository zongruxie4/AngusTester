package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncBaselineCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "FuncBaselineCase", description = "Test Baseline-Case Associations - Interface for managing relationships between functional test baselines and corresponding test cases.")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineCaseRest {

  @Resource
  private FuncBaselineCaseFacade funcBaselineCaseFacade;

  @Operation(summary = "Add the baseline of test cases", operationId = "func:baseline:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{baselineId}/case")
  public ApiLocaleResult<?> add(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.add(baselineId, caseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the baseline of test cases", operationId = "func:baseline:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{baselineId}/case")
  public void delete(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.delete(baselineId, caseIds);
  }

  @Operation(summary = "Query the baseline detail of test cases", operationId = "func:baseline:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{baselineId}/case/{caseId}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Parameter(name = "caseId", description = "Case ID", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.detail(baselineId, caseId));
  }

  @Operation(summary = "Query the baseline list of test cases", operationId = "func:baseline:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{baselineId}/case")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @ParameterObject FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.list(baselineId, dto));
  }

  @Operation(summary = "Fulltext search list the of baseline test cases", operationId = "func:baseline:case:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{baselineId}/case/search")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> search(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @ParameterObject FuncCaseSearchDto dto) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.search(baselineId, false, dto));
  }

  @Operation(summary = "Export the test cases of baseline", operationId = "func:baseline:case:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/{baselineId}/case/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "baselineId", description = "Baseline ID", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @ParameterObject FuncCaseSearchDto dto, HttpServletResponse response) {
    return funcBaselineCaseFacade.export(baselineId, dto, response);
  }

}
