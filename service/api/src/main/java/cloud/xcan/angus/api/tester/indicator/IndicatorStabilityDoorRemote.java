package cloud.xcan.angus.api.tester.indicator;

import static cloud.xcan.angus.spec.experimental.BizConstant.Header.OPT_TENANT_ID;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.tester.indicator.vo.StabilityVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface IndicatorStabilityDoorRemote {

  @Operation(description = "Query the indicator audit detail of stability", operationId = "indicator:stability:detailOrDefault:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/innerapi/v1/indicator/{targetType}/{targetId}/stability/detailOrDefault")
  ApiLocaleResult<StabilityVo> detailOrDefault(
      @RequestHeader(name = OPT_TENANT_ID) Long optTenantId,
      @Parameter(name = "targetType", description = "Target Type, allowable values: API,SCENARIO", required = true) @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId);

}
