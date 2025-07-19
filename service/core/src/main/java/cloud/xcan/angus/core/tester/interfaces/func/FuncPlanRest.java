package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncPlanFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.plan.FuncPlanDetailVo;
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

@Tag(name = "FuncPlan", description = "Test Plan Configuration - Core interface for creating/maintaining test plans that define testing scope, methodology, resources, etc")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanRest {

  @Resource
  private FuncPlanFacade funcPlanFacade;

  @Operation(summary = "Add the plan of functional testing", operationId = "func:plan:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncPlanAddDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.add(dto));
  }

  @Operation(summary = "Update the plan of functional testing", operationId = "func:plan:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncPlanUpdateDto dto) {
    funcPlanFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the plan of functional testing", operationId = "func:plan:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody FuncPlanReplaceDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.replace(dto));
  }

  @Operation(summary = "Start the plan of functional testing", operationId = "func:plan:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.start(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "End the plan of functional testing", operationId = "func:plan:end")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "End successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.end(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Block the plan of functional testing", operationId = "func:plan:block")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Block successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.block(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone the plan of functional testing", operationId = "func:plan:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.clone(id));
  }

  @Operation(summary = "Reset the test result of testing plan", operationId = "func:plan:case:test:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/case/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset the review result of testing plan", operationId = "func:plan:case:review:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the plan of functional testing", operationId = "func:plan:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.delete(id);
  }

  @Operation(summary = "Query the detail of testing plan", operationId = "func:plan:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncPlanDetailVo> detail(
      @Parameter(name = "id", description = "Plan id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.detail(id));
  }

  @Operation(summary = "Query the list of testing plan", operationId = "func:plan:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncPlanDetailVo>> list(
      @Valid @ParameterObject FuncPlanFindDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.list(dto));
  }

  @Operation(summary = "Query the not reviewed case list of testing plan", operationId = "func:plan:case:notReviewed:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{planId}/case/notReviewed")
  public ApiLocaleResult<List<FuncCaseListVo>> notReviewed(
      @Parameter(name = "planId", description = "Plan id", required = true) @PathVariable("planId") Long planId,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "reviewId", description = "Review id", required = false) @RequestParam(value = "reviewId", required = false) Long reviewId) {
    return ApiLocaleResult.success(funcPlanFacade.notReviewed(planId, moduleId, reviewId));
  }

  @Operation(summary = "Query the not established baseline case list of functional testing plan", operationId = "func:plan:case:notEstablishedBaseline:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{planId}/case/notEstablishedBaseline")
  public ApiLocaleResult<List<FuncCaseListVo>> notEstablishedBaseline(
      @Parameter(name = "planId", description = "Plan id", required = true) @PathVariable("planId") Long planId,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "baselineId", description = "Baseline id", required = false) @RequestParam(value = "baselineId", required = false) Long baselineId) {
    return ApiLocaleResult.success(
        funcPlanFacade.notEstablishedBaseline(planId, moduleId, baselineId));
  }

}
