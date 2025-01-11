package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncReviewCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "FuncReviewCase")
@Validated
@RestController
@RequestMapping("/api/v1/func/review/case")
public class FuncReviewCaseRest {

  @Resource
  private FuncReviewCaseFacade funcReviewCaseFacade;

  @ApiOperation(value = "Add functional testing review cases", nickname = "func:review:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody FuncReviewCaseAddDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.add(dto));
  }

  @ApiOperation(value = "Review functional test cases", nickname = "func:review:case:review")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/review")
  public ApiLocaleResult<?> review(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncReviewCaseDto> dto) {
    funcReviewCaseFacade.review(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reset the result of review case result", nickname = "func:review:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the result of review case result", nickname = "func:review:case:result:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/restart")
  public ApiLocaleResult<?> reviewStart(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewCaseFacade.reviewStart(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete functional testing cases of review", nickname = "func:review:case:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_OPT_CASE_NUM) @RequestBody HashSet<Long> ids) {
    funcReviewCaseFacade.delete(ids);
  }

  @ApiOperation(value = "Query the functional testing case detail of review", nickname = "func:review:case:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewCaseDetailVo> detail(
      @ApiParam(name = "id", value = "Review case id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewCaseFacade.detail(id));
  }

  @ApiOperation(value = "Query the functional case info list of review", nickname = "func:review:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewCaseVo>> list(@Valid FuncReviewCaseFindDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext the functional case info list of review", nickname = "func:review:case:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncReviewCaseVo>> search(@Valid FuncReviewCaseSearchDto dto) {
    return ApiLocaleResult.success(funcReviewCaseFacade.search(dto));
  }

}
