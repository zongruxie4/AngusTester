package cloud.xcan.sdf.core.angustester.interfaces.indicator;


import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.PerfVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorPerfFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "IndicatorPerfDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1/indicator")
public class IndicatorPerfDoorRest {

  @Resource
  private IndicatorPerfFacade perfFacade;

  @ApiOperation(value = "Query the indicator detail of performance, return to default configuration when not set", nickname = "indicator:perf:detailOrDefault:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/perf/detailOrDefault")
  public ApiLocaleResult<PerfVo> detailOrDefault(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(perfFacade.detailOrDefault(targetType, targetId));
  }

}