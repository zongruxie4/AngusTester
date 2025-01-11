package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncPlanFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.FuncPlanAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.FuncPlanFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.FuncPlanReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.FuncPlanSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.FuncPlanUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.plan.FuncPlanDetailVo;
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

@Api(tags = "FuncPlan")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanRest {

  @Resource
  private FuncPlanFacade funcPlanFacade;

  @ApiOperation(value = "Add functional testing plan", nickname = "func:plan:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncPlanAddDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.add(dto));
  }

  @ApiOperation(value = "Update functional testing plan", nickname = "func:plan:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncPlanUpdateDto dto) {
    funcPlanFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace functional testing plan", nickname = "func:plan:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody FuncPlanReplaceDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.replace(dto));
  }

  @ApiOperation(value = "Start functional testing plan", nickname = "func:plan:start")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Started successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.start(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "End functional testing plan", nickname = "func:plan:end")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "End successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.end(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Block functional testing plan", nickname = "func:plan:block")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Block successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.block(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone functional testing plan", nickname = "func:plan:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.clone(id));
  }

  @ApiOperation(value = "Reset the result of test result", nickname = "func:plan:case:test:result:reset")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/case/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reset the result of review result", nickname = "func:plan:case:review:result:reset")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcPlanFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete functional testing plan", nickname = "func:plan:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    funcPlanFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of functional testing plan", nickname = "func:plan:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncPlanDetailVo> detail(
      @ApiParam(name = "id", value = "Plan id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcPlanFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of functional testing plan", nickname = "func:plan:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncPlanDetailVo>> list(@Valid FuncPlanFindDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of functional testing plan", nickname = "func:plan:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncPlanDetailVo>> search(@Valid FuncPlanSearchDto dto) {
    return ApiLocaleResult.success(funcPlanFacade.search(dto));
  }

  @ApiOperation(value = "Query the not reviewed case list of functional testing plan", nickname = "func:plan:case:notReviewed:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{planId}/case/notReviewed")
  public ApiLocaleResult<List<FuncCaseListVo>> notReviewed(
      @ApiParam(name = "planId", value = "Plan id", required = true) @PathVariable("planId") Long planId,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @ApiParam(name = "reviewId", value = "Review id", required = false) @RequestParam(value = "reviewId", required = false) Long reviewId) {
    return ApiLocaleResult.success(funcPlanFacade.notReviewed(planId, moduleId, reviewId));
  }

  @ApiOperation(value = "Query the not established baseline case list of functional testing plan", nickname = "func:plan:case:notEstablishedBaseline:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{planId}/case/notEstablishedBaseline")
  public ApiLocaleResult<List<FuncCaseListVo>> notEstablishedBaseline(
      @ApiParam(name = "planId", value = "Plan id", required = true) @PathVariable("planId") Long planId,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @ApiParam(name = "baselineId", value = "Baseline id", required = false) @RequestParam(value = "baselineId", required = false) Long baselineId) {
    return ApiLocaleResult.success(
        funcPlanFacade.notEstablishedBaseline(planId, moduleId, baselineId));
  }

}
