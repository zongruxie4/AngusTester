package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncReviewCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Functional Test Review-Case", description = "Functional Test Review-Case Management - APIs for linking test reviews to specific test cases with review process management and result tracking")
@Validated
@RestController
@RequestMapping("/api/v1/func/review/case")
public class FuncReviewCaseRest {

  @Resource
  private FuncReviewCaseFacade funcReviewCaseFacade;

  @Operation(summary = "Add test cases to review", 
      description = "Associate multiple test cases with a specific review for systematic analysis and discussion",
      operationId = "func:review:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test cases added to review successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody FuncReviewCaseAddDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.add(dto));
  }

  @Operation(summary = "Review test cases", 
      description = "Perform review operations on multiple test cases with comprehensive review feedback and results",
      operationId = "func:review:case:review")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test cases reviewed successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PutMapping("/review")
  public ApiLocaleResult<?> review(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncReviewCaseDto> dto) {
    funcReviewCaseFacade.review(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset test case review results", 
      description = "Reset review results for multiple test cases to initial state for re-review",
      operationId = "func:review:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review results reset successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart test case review results", 
      description = "Restart review process for multiple test cases to initiate new review cycle",
      operationId = "func:review:case:result:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review results restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/restart")
  public ApiLocaleResult<?> reviewStart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewStart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Remove test cases from review", 
      description = "Disassociate multiple test cases from a specific review to remove from review process",
      operationId = "func:review:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test cases removed from review successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> ids) {
    funcReviewCaseFacade.delete(ids);
  }

  @Operation(summary = "Get review test case details", 
      description = "Retrieve comprehensive details of a specific test case within a review for analysis",
      operationId = "func:review:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Review test case details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Review test case not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewCaseDetailVo> detail(
      @Parameter(name = "id", description = "Review test case identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewCaseFacade.detail(id));
  }

  @Operation(summary = "List review test cases", 
      description = "Retrieve paginated list of test cases associated with reviews with comprehensive filtering options",
      operationId = "func:review:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Review test case list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewCaseVo>> list(
      @Valid @ParameterObject FuncReviewCaseFindDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.list(dto));
  }
}
