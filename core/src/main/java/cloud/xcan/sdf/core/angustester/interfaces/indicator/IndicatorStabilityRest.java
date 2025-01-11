package cloud.xcan.sdf.core.angustester.interfaces.indicator;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.StabilityVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.sdf.spec.annotations.Unused;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "IndicatorStability")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorStabilityRest {

  @Resource
  private IndicatorStabilityFacade stabilityFacade;

  @Unused
  @ApiOperation(value = "Add the indicator of stability", nickname = "indicator:stability:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/stability")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody StabilityAddDto dto) {
    return ApiLocaleResult.success(stabilityFacade.add(dto));
  }

  @ApiOperation(value = "Replace the indicator of stability", nickname = "indicator:stability:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/stability")
  public ApiLocaleResult<?> replace(@Valid @RequestBody StabilityReplaceDto dto) {
    stabilityFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the stability indicator", nickname = "indicator:stability:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/stability")
  public void delete(
      @ApiParam(name = "ids", value = "Stability indicator id", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    stabilityFacade.delete(ids);
  }

  @ApiOperation(value = "Delete the stability indicator of target", nickname = "indicator:stability:target:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/stability")
  public void deleteByTarget(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    stabilityFacade.deleteByTarget(targetType, targetId);
  }

  @ApiOperation(value = "Query the indicator detail of stability", nickname = "indicator:stability:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/stability")
  public ApiLocaleResult<StabilityVo> detail(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(stabilityFacade.detail(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator detail of stability, return to default configuration when not set", nickname = "indicator:stability:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/stability/detailOrDefault")
  public ApiLocaleResult<StabilityVo> detailOrDefault(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(stabilityFacade.detailOrDefault(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator list of stability", nickname = "indicator:stability:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/stability")
  public ApiLocaleResult<PageResult<StabilityListVo>> list(@Valid StabilityFindDto dto) {
    return ApiLocaleResult.success(stabilityFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the indicator of stability", nickname = "indicator:stability:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/stability/search")
  public ApiLocaleResult<PageResult<StabilityListVo>> search(@Valid StabilitySearchDto dto) {
    return ApiLocaleResult.success(stabilityFacade.search(dto));
  }

}