package cloud.xcan.angus.core.tester.interfaces.indicator;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.Unused;
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

@Tag(name = "IndicatorStability", description = "Stability Test Indicator Management - Configure stability test profiles (duration, concurrency, error tolerance).")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorStabilityRest {

  @Resource
  private IndicatorStabilityFacade indicatorStabilityFacade;

  @Unused
  @Operation(description = "Add the indicator of stability", operationId = "indicator:stability:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/stability")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody StabilityAddDto dto) {
    return ApiLocaleResult.success(indicatorStabilityFacade.add(dto));
  }

  @Operation(description = "Replace the indicator of stability", operationId = "indicator:stability:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/stability")
  public ApiLocaleResult<?> replace(@Valid @RequestBody StabilityReplaceDto dto) {
    indicatorStabilityFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the stability indicator", operationId = "indicator:stability:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/stability")
  public void delete(
      @Parameter(name = "ids", description = "Stability indicator id", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    indicatorStabilityFacade.delete(ids);
  }

  @Operation(description = "Delete the stability indicator of target", operationId = "indicator:stability:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/stability")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    indicatorStabilityFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(description = "Query the indicator detail of stability", operationId = "indicator:stability:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/stability")
  public ApiLocaleResult<StabilityVo> detail(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorStabilityFacade.detail(targetType, targetId));
  }

  @Operation(description = "Query the indicator detail of stability, return to default configuration when not set", operationId = "indicator:stability:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/stability/detailOrDefault")
  public ApiLocaleResult<StabilityVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorStabilityFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(description = "Query the indicator list of stability", operationId = "indicator:stability:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/stability")
  public ApiLocaleResult<PageResult<StabilityListVo>> list(@Valid StabilityFindDto dto) {
    return ApiLocaleResult.success(indicatorStabilityFacade.list(dto));
  }

  @Operation(description = "Fulltext search the indicator of stability", operationId = "indicator:stability:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/stability/search")
  public ApiLocaleResult<PageResult<StabilityListVo>> search(@Valid StabilitySearchDto dto) {
    return ApiLocaleResult.success(indicatorStabilityFacade.search(dto));
  }

}
