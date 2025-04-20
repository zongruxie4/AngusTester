package cloud.xcan.angus.core.tester.interfaces.indicator;


import cloud.xcan.angus.api.tester.indicator.vo.PerfVo;
import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfSearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

@Api(tags = "IndicatorPerf")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorPerfRest {

  @Resource
  private IndicatorPerfFacade perfFacade;

  @Operation(description = "Add the indicator of performance", operationId = "indicator:perf:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/perf")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody PerfAddDto dto) {
    return ApiLocaleResult.success(perfFacade.add(dto));
  }

  @Operation(description = "Replace the indicator of performance or throw 404 when it doesn't exist", operationId = "indicator:perf:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/perf")
  public ApiLocaleResult<?> replace(@Valid @RequestBody PerfReplaceDto dto) {
    perfFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the performance indicator of target", operationId = "indicator:perf:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/perf")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    perfFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(description = "Query the indicator detail of performance", operationId = "indicator:perf:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/perf")
  public ApiLocaleResult<PerfVo> detail(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(perfFacade.detail(targetType, targetId));
  }

  @Operation(description = "Query the indicator detail of performance, return to default configuration when not set", operationId = "indicator:perf:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/perf/detailOrDefault")
  public ApiLocaleResult<PerfVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(perfFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(description = "Query the indicator list of performance", operationId = "indicator:perf:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/perf")
  public ApiLocaleResult<PageResult<PerfListVo>> list(@Valid PerfFindDto dto) {
    return ApiLocaleResult.success(perfFacade.list(dto));
  }

  @Operation(description = "Fulltext search the indicator of performance", operationId = "indicator:perf:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/perf/search")
  public ApiLocaleResult<PageResult<PerfListVo>> search(@Valid PerfSearchDto dto) {
    return ApiLocaleResult.success(perfFacade.search(dto));
  }

}
