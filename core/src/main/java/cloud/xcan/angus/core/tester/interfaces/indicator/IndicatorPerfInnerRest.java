package cloud.xcan.angus.core.tester.interfaces.indicator;



import cloud.xcan.angus.api.tester.indicator.vo.PerfVo;
import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "IndicatorPerfInner", description = "Performance Test Indicator Query (Internal) - Internal endpoints for AngusCtrl system to retrieve performance testing indicator.")
@Validated
@RestController
@RequestMapping("/innerapi/v1/indicator")
public class IndicatorPerfInnerRest {

  @Resource
  private IndicatorPerfFacade indicatorPerfFacade;

  @Operation(description = "Query the indicator detail of performance, return to default configuration when not set", operationId = "indicator:perf:detailOrDefault:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/perf/detailOrDefault")
  public ApiLocaleResult<PerfVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorPerfFacade.detailOrDefault(targetType, targetId));
  }

}
