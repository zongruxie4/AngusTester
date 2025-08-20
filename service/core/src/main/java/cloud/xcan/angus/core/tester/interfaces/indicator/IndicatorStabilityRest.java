package cloud.xcan.angus.core.tester.interfaces.indicator;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityReplaceDto;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Indicator - Stability Test", description = "Stability Test Indicator Management - APIs for configuring stability test profiles (duration, concurrency, error tolerance) with target-based configuration and batch operations")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorStabilityRest {

  @Resource
  private IndicatorStabilityFacade indicatorStabilityFacade;

  @Unused
  @Operation(summary = "Create stability test indicator", 
      description = "Create a new stability test indicator with configuration for stability parameters",
      operationId = "indicator:stability:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Stability test indicator created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/stability")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody StabilityAddDto dto) {
    return ApiLocaleResult.success(indicatorStabilityFacade.add(dto));
  }

  @Operation(summary = "Replace stability test indicator", 
      description = "Replace an existing stability test indicator with new configuration",
      operationId = "indicator:stability:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stability test indicator replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Stability test indicator not found")
  })
  @PutMapping("/stability")
  public ApiLocaleResult<?> replace(@Valid @RequestBody StabilityReplaceDto dto) {
    indicatorStabilityFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete stability test indicators", 
      description = "Delete multiple stability test indicators with batch operation support",
      operationId = "indicator:stability:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Stability test indicators deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/stability")
  public void delete(
      @Parameter(name = "ids", description = "Stability indicator identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    indicatorStabilityFacade.delete(ids);
  }

  @Operation(summary = "Delete stability test indicator by target", 
      description = "Remove stability test indicator configuration for a specific target type and identifier",
      operationId = "indicator:stability:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Stability test indicator deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/stability")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target type for indicator configuration (API, SCENARIO)", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator configuration", required = true) @PathVariable("targetId") Long targetId) {
    indicatorStabilityFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(summary = "Get stability test indicator details", 
      description = "Retrieve details of stability test indicator for a specific target",
      operationId = "indicator:stability:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stability test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Stability test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/stability")
  public ApiLocaleResult<StabilityVo> detail(
      @Parameter(name = "targetType", description = "Target type for indicator query (API, SCENARIO)", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorStabilityFacade.detail(targetType, targetId));
  }

  @Operation(summary = "Get stability test indicator details or default", 
      description = "Retrieve stability test indicator details for a specific target, returning default configuration when not set",
      operationId = "indicator:stability:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stability test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Stability test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/stability/detailOrDefault")
  public ApiLocaleResult<StabilityVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target type for indicator query (API, SCENARIO)", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorStabilityFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(summary = "List stability test indicators", 
      description = "Retrieve paginated list of stability test indicators with filtering and search options",
      operationId = "indicator:stability:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stability test indicator list retrieved successfully")})
  @GetMapping("/stability")
  public ApiLocaleResult<PageResult<StabilityListVo>> list(@Valid @ParameterObject StabilityFindDto dto) {
    return ApiLocaleResult.success(indicatorStabilityFacade.list(dto));
  }

}
