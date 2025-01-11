package cloud.xcan.sdf.api.angustester.scenario;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioListVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xiaolong.liu
 */
@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ScenarioRemote {

  @ApiOperation(value = "Query scenario list", nickname = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/api/v1/scenario/list")
  ApiLocaleResult<List<ScenarioListVo>> list(
      @ApiParam(name = "ids", value = "Scenario ids", required = true) @Valid @RequestParam("ids") @Size(max = DEFAULT_BATCH_SIZE) Set<Long> ids);

  @ApiOperation(value = "Query scenario detail", nickname = "scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/api/v1/scenario/{id}")
  ApiLocaleResult<ScenarioDetailVo> detail(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id);

}
