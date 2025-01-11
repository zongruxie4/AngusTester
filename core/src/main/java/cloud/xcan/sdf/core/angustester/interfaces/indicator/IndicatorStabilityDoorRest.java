package cloud.xcan.sdf.core.angustester.interfaces.indicator;


import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.StabilityVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorStabilityFacade;
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

@Api(tags = "IndicatorStabilityDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1/indicator")
public class IndicatorStabilityDoorRest {

  @Resource
  private IndicatorStabilityFacade stabilityFacade;

  @ApiOperation(value = "Query the indicator detail of stability, return to default configuration when not set", nickname = "indicator:stability:detailOrDefault:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/stability/detailOrDefault")
  public ApiLocaleResult<StabilityVo> detailOrDefault(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(stabilityFacade.detailOrDefault(targetType, targetId));
  }

}