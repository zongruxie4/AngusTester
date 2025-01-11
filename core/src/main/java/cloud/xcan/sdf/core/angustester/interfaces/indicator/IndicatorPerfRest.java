package cloud.xcan.sdf.core.angustester.interfaces.indicator;


import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.PerfVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

  @ApiOperation(value = "Add the indicator of performance", nickname = "indicator:perf:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/perf")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody PerfAddDto dto) {
    return ApiLocaleResult.success(perfFacade.add(dto));
  }

  @ApiOperation(value = "Replace the indicator of performance or throw 404 when it doesn't exist", nickname = "indicator:perf:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/perf")
  public ApiLocaleResult<?> replace(@Valid @RequestBody PerfReplaceDto dto) {
    perfFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the performance indicator of target", nickname = "indicator:perf:target:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/perf")
  public void deleteByTarget(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    perfFacade.deleteByTarget(targetType, targetId);
  }

  @ApiOperation(value = "Query the indicator detail of performance", nickname = "indicator:perf:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/perf")
  public ApiLocaleResult<PerfVo> detail(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(perfFacade.detail(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator detail of performance, return to default configuration when not set", nickname = "indicator:perf:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/perf/detailOrDefault")
  public ApiLocaleResult<PerfVo> detailOrDefault(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(perfFacade.detailOrDefault(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator list of performance", nickname = "indicator:perf:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/perf")
  public ApiLocaleResult<PageResult<PerfListVo>> list(@Valid PerfFindDto dto) {
    return ApiLocaleResult.success(perfFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the indicator of performance", nickname = "indicator:perf:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/perf/search")
  public ApiLocaleResult<PageResult<PerfListVo>> search(@Valid PerfSearchDto dto) {
    return ApiLocaleResult.success(perfFacade.search(dto));
  }

}