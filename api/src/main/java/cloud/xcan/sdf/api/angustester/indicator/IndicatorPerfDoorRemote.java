package cloud.xcan.sdf.api.angustester.indicator;

import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.OPT_TENANT_ID;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.PerfVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface IndicatorPerfDoorRemote {

  @ApiOperation(value = "Query the indicator audit detail of performance", nickname = "indicator:perf:detailOrDefault:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/doorapi/v1/indicator/{targetType}/{targetId}/perf/detailOrDefault")
  ApiLocaleResult<PerfVo> detailOrDefault(
      @RequestHeader(name = OPT_TENANT_ID) Long optTenantId,
      @PathVariable("targetType") @ApiParam(name = "targetType", value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true) CombinedTargetType targetType,
      @PathVariable("targetId") @ApiParam(name = "targetId", value = "Target id", required = true) Long targetId);

}
