package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncReviewCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseSearchDto;
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

@Tag(name = "FuncReviewCase", description = "Test Review-Case Relationships - Management interface for linking test review to specific test cases")
@Validated
@RestController
@RequestMapping("/api/v1/func/review/case")
public class FuncReviewCaseRest {

  @Resource
  private FuncReviewCaseFacade funcReviewCaseFacade;

  @Operation(summary = "Add functional testing review cases", operationId = "func:review:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody FuncReviewCaseAddDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.add(dto));
  }

  @Operation(summary = "Review functional test cases", operationId = "func:review:case:review")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/review")
  public ApiLocaleResult<?> review(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncReviewCaseDto> dto) {
    funcReviewCaseFacade.review(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset the review result of case", operationId = "func:review:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart the result of review case result", operationId = "func:review:case:result:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/restart")
  public ApiLocaleResult<?> reviewStart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewStart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete functional testing cases of review", operationId = "func:review:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> ids) {
    funcReviewCaseFacade.delete(ids);
  }

  @Operation(summary = "Query the functional testing case detail of review", operationId = "func:review:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewCaseDetailVo> detail(
      @Parameter(name = "id", description = "Review case id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewCaseFacade.detail(id));
  }

  @Operation(summary = "Query the functional case info list of review", operationId = "func:review:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewCaseVo>> list(
      @Valid @ParameterObject FuncReviewCaseFindDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.list(dto));
  }

  @Operation(summary = "Fulltext the functional case info list of review", operationId = "func:review:case:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncReviewCaseVo>> search(
      @Valid @ParameterObject FuncReviewCaseSearchDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.search(dto));
  }

}
