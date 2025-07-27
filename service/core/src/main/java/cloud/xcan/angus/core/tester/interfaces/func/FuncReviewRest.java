package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncReviewFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.FuncReviewReplaceDto;
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

@Tag(name = "Functional Test Review", description = "Functional Test Review Management - Comprehensive APIs for systematic analysis and discussion of test cases through formal reviews with process management and lifecycle control")
@Validated
@RestController
@RequestMapping("/api/v1/func/review")
public class FuncReviewRest {

  @Resource
  private FuncReviewFacade funcReviewFacade;

  @Operation(summary = "Create functional test review", 
      description = "Create a new functional test review with comprehensive configuration for systematic analysis",
      operationId = "func:review:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test review created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncReviewAddDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.add(dto));
  }

  @Operation(summary = "Update functional test review", 
      description = "Update an existing functional test review with partial modification support",
      operationId = "func:review:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review updated successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncReviewUpdateDto dto) {
    funcReviewFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace functional test review", 
      description = "Replace an existing functional test review with complete new configuration",
      operationId = "func:review:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncReviewReplaceDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.replace(dto));
  }

  @Operation(summary = "Start functional test review", 
      description = "Activate a functional test review to begin systematic analysis and discussion process",
      operationId = "func:review:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review started successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Review identifier for start operation", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "End functional test review", 
      description = "Complete a functional test review to finalize analysis and discussion process",
      operationId = "func:review:end")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review ended successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Review identifier for end operation", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Block functional test review", 
      description = "Suspend a functional test review to pause analysis and discussion process",
      operationId = "func:review:block")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review blocked successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Review identifier for block operation", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone functional test review", 
      description = "Create a copy of an existing functional test review with all configurations and settings",
      operationId = "func:review:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Review identifier for clone operation", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.clone(id));
  }

  @Operation(summary = "Reset test case review results", 
      description = "Reset review results for multiple test cases within a review for re-review",
      operationId = "func:review:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review results reset successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart test case review results", 
      description = "Restart review process for multiple test cases within a review to initiate new review cycle",
      operationId = "func:review:result:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review results restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/case/review/restart")
  public ApiLocaleResult<?> reviewRestart(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewRestart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete functional test review", 
      description = "Remove a functional test review and all associated test cases and configurations",
      operationId = "func:review:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Functional test review deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Review identifier for deletion", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.delete(id);
  }

  @Operation(summary = "Get functional test review details", 
      description = "Retrieve comprehensive details of a specific functional test review for analysis and review",
      operationId = "func:review:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test review not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewDetailVo> detail(
      @Parameter(name = "id", description = "Review identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.detail(id));
  }

  @Operation(summary = "List functional test reviews", 
      description = "Retrieve paginated list of functional test reviews with comprehensive filtering and search options",
      operationId = "func:review:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test review list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewDetailVo>> list(
      @Valid @ParameterObject FuncReviewFindDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.list(dto));
  }

}
