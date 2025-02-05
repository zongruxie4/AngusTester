package cloud.xcan.sdf.core.angustester.interfaces.data;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DataParameterizationFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "DataParameterization")
@Validated
@RestController
@RequestMapping("/api/v1/target")
public class DataParameterizationRest {

  @Resource
  private DataParameterizationFacade dataParameterizationFacade;

  @ApiOperation(value = "Preview the variables and datasets values of API, API_CASE, SCENARIO", nickname = "target:parameter:data:value")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{targetId}/{targetType}/parameter/data/value")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(dataParameterizationFacade.valuePreview(targetId, targetType));
  }

}
