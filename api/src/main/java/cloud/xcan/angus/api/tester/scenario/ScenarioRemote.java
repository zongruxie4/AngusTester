package cloud.xcan.angus.api.tester.scenario;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.tester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.angus.api.tester.scenario.vo.ScenarioListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author XiaoLong Liu
 */@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ScenarioRemote {

  @Operation(description = "Query scenario list", operationId = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/api/v1/scenario/list")
  ApiLocaleResult<List<ScenarioListVo>> list(
      @Parameter(name = "ids", description = "Scenario ids", required = true) @Valid @RequestParam("ids") @Size(max = MAX_BATCH_SIZE) Set<Long> ids);

  @Operation(description = "Query scenario detail", operationId = "scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/api/v1/scenario/{id}")
  ApiLocaleResult<ScenarioDetailVo> detail(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id);

}
