package cloud.xcan.angus.core.tester.interfaces.test;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncPlanFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.FuncPlanAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.FuncPlanFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.FuncPlanReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.FuncPlanUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.plan.FuncPlanDetailVo;
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

@Tag(name = "Functional Test Plan", description = "Functional Test Plan Management - APIs for creating, maintaining, and managing test plans that define testing scope, methodology, resources, and lifecycle control")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanRest {

  @Resource
  private FuncPlanFacade funcPlanFacade;

  @Operation(summary = "Create functional test plan",
      description = "Create a new functional test plan with configuration for testing scope and methodology",
      operationId = "func:plan:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test plan created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncPlanAddDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.add(dto));
  }

  @Operation(summary = "Update functional test plan",
      description = "Update an existing functional test plan with partial modification support",
      operationId = "func:plan:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan updated successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncPlanUpdateDto dto) {
    funcPlanFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace functional test plan",
      description = "Replace an existing functional test plan with new configuration",
      operationId = "func:plan:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody FuncPlanReplaceDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.replace(dto));
  }

  @Operation(summary = "Start functional test plan",
      description = "Activate a functional test plan to begin test execution and management",
      operationId = "func:plan:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan started successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Test plan identifier for start operation", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "End functional test plan",
      description = "Complete a functional test plan to finalize test execution and results",
      operationId = "func:plan:end")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan ended successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Test plan identifier for end operation", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Block functional test plan",
      description = "Suspend a functional test plan to pause test execution and management",
      operationId = "func:plan:block")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan blocked successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Test plan identifier for block operation", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone functional test plan",
      description = "Create a copy of an existing functional test plan with all configurations and settings",
      operationId = "func:plan:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Test plan identifier for clone operation", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.clone(id));
  }

  @Operation(summary = "Reset test case results",
      description = "Reset test results for multiple test cases within a test plan for re-execution",
      operationId = "func:plan:case:test:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case results reset successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/case/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset test case review results",
      description = "Reset review results for multiple test cases within a test plan for re-review",
      operationId = "func:plan:case:review:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review results reset successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete functional test plan",
      description = "Remove a functional test plan and all associated test cases and configurations",
      operationId = "func:plan:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Functional test plan deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Test plan identifier for deletion", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.delete(id);
  }

  @Operation(summary = "Get functional test plan details",
      description = "Retrieve details of a specific functional test plan for analysis and review",
      operationId = "func:plan:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test plan not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncPlanDetailVo> detail(
      @Parameter(name = "id", description = "Test plan identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.detail(id));
  }

  @Operation(summary = "List functional test plans",
      description = "Retrieve paginated list of functional test plans with filtering and search options",
      operationId = "func:plan:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test plan list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncPlanDetailVo>> list(
      @Valid @ParameterObject FuncPlanFindDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.list(dto));
  }

  @Operation(summary = "Get unreviewed test cases",
      description = "Retrieve list of test cases that have not been reviewed within a specific test plan",
      operationId = "func:plan:case:notReviewed:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unreviewed test cases retrieved successfully")})
  @GetMapping(value = "/{planId}/case/notReviewed")
  public ApiLocaleResult<List<FuncCaseListVo>> notReviewed(
      @Parameter(name = "planId", description = "Test plan identifier for unreviewed case query", required = true) @PathVariable("planId") Long planId,
      @Parameter(name = "moduleId", description = "Module identifier for case filtering", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "reviewId", description = "Review identifier for specific review filtering", required = false) @RequestParam(value = "reviewId", required = false) Long reviewId) {
    return ApiLocaleResult.success(funcPlanFacade.notReviewed(planId, moduleId, reviewId));
  }

  @Operation(summary = "Get test cases without established baseline",
      description = "Retrieve list of test cases that do not have established baselines within a specific test plan",
      operationId = "func:plan:case:notEstablishedBaseline:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test cases without established baseline retrieved successfully")})
  @GetMapping(value = "/{planId}/case/notEstablishedBaseline")
  public ApiLocaleResult<List<FuncCaseListVo>> notEstablishedBaseline(
      @Parameter(name = "planId", description = "Test plan identifier for baseline case query", required = true) @PathVariable("planId") Long planId,
      @Parameter(name = "moduleId", description = "Module identifier for case filtering", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "baselineId", description = "Baseline identifier for specific baseline filtering", required = false) @RequestParam(value = "baselineId", required = false) Long baselineId) {
    return ApiLocaleResult.success(
        funcPlanFacade.notEstablishedBaseline(planId, moduleId, baselineId));
  }

}
