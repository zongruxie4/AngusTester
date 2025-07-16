package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncReviewFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewDetailVo;
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
import jakarta.validation.constraints.Size;
import java.util.HashSet;
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

@Tag(name = "FuncReview", description = "Test Review Management - Process management interface for systematic analysis and discussion of test cases through formal reviews")
@Validated
@RestController
@RequestMapping("/api/v1/func/review")
public class FuncReviewRest {

  @Resource
  private FuncReviewFacade funcReviewFacade;

  @Operation(summary = "Add the review of functional testing", operationId = "func:review:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncReviewAddDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.add(dto));
  }

  @Operation(summary = "Update the review of functional testing", operationId = "func:review:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncReviewUpdateDto dto) {
    funcReviewFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the review of functional testing", operationId = "func:review:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncReviewReplaceDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.replace(dto));
  }

  @Operation(summary = "Start the review of functional testing", operationId = "func:review:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "End the review of functional testing", operationId = "func:review:end")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "End successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Block the review of functional testing", operationId = "func:review:block")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Block successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone the review of functional testing", operationId = "func:review:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.clone(id));
  }

  @Operation(summary = "Reset the review result of functional testing", operationId = "func:review:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart the review result of functional testing", operationId = "func:review:result:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/case/review/restart")
  public ApiLocaleResult<?> reviewRestart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewRestart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the review of functional testing", operationId = "func:review:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.delete(id);
  }

  @Operation(summary = "Query the detail of testing review", operationId = "func:review:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewDetailVo> detail(
      @Parameter(name = "id", description = "Review id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.detail(id));
  }

  @Operation(summary = "Query the list of testing review", operationId = "func:review:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewDetailVo>> list(
      @Valid @ParameterObject FuncReviewFindDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of testing review", operationId = "func:review:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncReviewDetailVo>> search(
      @Valid @ParameterObject FuncReviewSearchDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.search(dto));
  }

}
