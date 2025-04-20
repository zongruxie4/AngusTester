package cloud.xcan.angus.core.tester.interfaces.indicator;



import cloud.xcan.angus.api.tester.indicator.vo.StabilityVo;
import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "IndicatorStabilityInner")
@Validated
@RestController
@RequestMapping("/innerapi/v1/indicator")
public class IndicatorStabilityDoorRest {

  @Resource
  private IndicatorStabilityFacade stabilityFacade;

  @Operation(description = "Query the indicator detail of stability, return to default configuration when not set", operationId = "indicator:stability:detailOrDefault:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/stability/detailOrDefault")
  public ApiLocaleResult<StabilityVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(stabilityFacade.detailOrDefault(targetType, targetId));
  }

}
