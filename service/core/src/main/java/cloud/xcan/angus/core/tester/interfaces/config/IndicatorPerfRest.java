package cloud.xcan.angus.core.tester.interfaces.config;


import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.config.facade.IndicatorPerfFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.PerfListVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.PerfVo;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Indicator - Performance Test", description = "Performance Test Indicator Management - APIs for defining and adjusting performance test parameters (concurrency levels, TPS targets, error rate thresholds) with target-based configuration")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorPerfRest {

  @Resource
  private IndicatorPerfFacade indicatorPerfFacade;

  @Operation(summary = "Create performance test indicator",
      description = "Create a new performance test indicator with configuration for performance parameters",
      operationId = "indicator:perf:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Performance test indicator created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/perf")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody PerfAddDto dto) {
    return ApiLocaleResult.success(indicatorPerfFacade.add(dto));
  }

  @Operation(summary = "Replace performance test indicator",
      description = "Replace an existing performance test indicator with new configuration",
      operationId = "indicator:perf:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Performance test indicator replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Performance test indicator not found")
  })
  @PutMapping("/perf")
  public ApiLocaleResult<?> replace(@Valid @RequestBody PerfReplaceDto dto) {
    indicatorPerfFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete performance test indicator by target",
      description = "Remove performance test indicator configuration for a specific target type and identifier",
      operationId = "indicator:perf:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Performance test indicator deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/perf")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target type for indicator configuration (API, SCENARIO)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator configuration", required = true) @PathVariable("targetId") Long targetId) {
    indicatorPerfFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(summary = "Get performance test indicator details",
      description = "Retrieve details of performance test indicator for a specific target",
      operationId = "indicator:perf:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Performance test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Performance test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/perf")
  public ApiLocaleResult<PerfVo> detail(
      @Parameter(name = "targetType", description = "Target type for indicator query (API, SCENARIO)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorPerfFacade.detail(targetType, targetId));
  }

  @Operation(summary = "Get performance test indicator details or default",
      description = "Retrieve performance test indicator details for a specific target, returning default configuration when not set",
      operationId = "indicator:perf:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Performance test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Performance test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/perf/detailOrDefault")
  public ApiLocaleResult<PerfVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target type for indicator query (API, SCENARIO)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorPerfFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(summary = "List performance test indicators",
      description = "Retrieve paginated list of performance test indicators with filtering and search options",
      operationId = "indicator:perf:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Performance test indicator list retrieved successfully")})
  @GetMapping("/perf")
  public ApiLocaleResult<PageResult<PerfListVo>> list(@Valid @ParameterObject PerfFindDto dto) {
    return ApiLocaleResult.success(indicatorPerfFacade.list(dto));
  }

}
