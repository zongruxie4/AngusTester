package cloud.xcan.angus.core.tester.interfaces.test;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;

import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncBaselineCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseListVo;
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

@Tag(name = "Functional Test Baseline-Case", description = "Functional Test Baseline-Case Management - APIs for managing relationships between functional test baselines and corresponding test cases with association control and export capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineCaseRest {

  @Resource
  private FuncBaselineCaseFacade funcBaselineCaseFacade;

  @Operation(summary = "Add test cases to baseline",
      description = "Associate multiple test cases with a specific baseline for version control and reference management",
      operationId = "func:baseline:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test cases added to baseline successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{baselineId}/case")
  public ApiLocaleResult<?> add(
      @Parameter(name = "baselineId", description = "Baseline identifier for case association", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.add(baselineId, caseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Remove test cases from baseline",
      description = "Disassociate multiple test cases from a specific baseline to remove version control references",
      operationId = "func:baseline:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test cases removed from baseline successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{baselineId}/case")
  public void delete(
      @Parameter(name = "baselineId", description = "Baseline identifier for case disassociation", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> caseIds) {
    funcBaselineCaseFacade.delete(baselineId, caseIds);
  }

  @Operation(summary = "Get baseline test case details",
      description = "Retrieve comprehensive details of a specific test case within a baseline for analysis and review",
      operationId = "func:baseline:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Baseline test case details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Baseline test case not found")})
  @GetMapping(value = "/{baselineId}/case/{caseId}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @Parameter(name = "baselineId", description = "Baseline identifier for case detail retrieval", required = true) @PathVariable("baselineId") Long baselineId,
      @Parameter(name = "caseId", description = "Test case identifier for detail retrieval", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.detail(baselineId, caseId));
  }

  @Operation(summary = "List baseline test cases",
      description = "Retrieve paginated list of test cases associated with a specific baseline with comprehensive filtering options",
      operationId = "func:baseline:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Baseline test case list retrieved successfully")})
  @GetMapping("/{baselineId}/case")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(
      @Parameter(name = "baselineId", description = "Baseline identifier for case list retrieval", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @ParameterObject FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcBaselineCaseFacade.list(false, baselineId, dto));
  }

  @Operation(summary = "Export baseline test cases",
      description = "Export test cases associated with a specific baseline in various formats for external analysis and reporting",
      operationId = "func:baseline:case:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Baseline test cases exported successfully")
  })
  @GetMapping(value = "/{baselineId}/case/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "baselineId", description = "Baseline identifier for case export", required = true) @PathVariable("baselineId") Long baselineId,
      @Valid @ParameterObject FuncCaseFindDto dto, HttpServletResponse response) {
    return funcBaselineCaseFacade.export(baselineId, dto, response);
  }

}
