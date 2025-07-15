package cloud.xcan.angus.core.tester.interfaces.data;


import cloud.xcan.angus.core.tester.interfaces.data.facade.DataParameterizationFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DataParameterization", description = "Data Parameterization Preview - Dedicated access for previewing parameterized data values during test configuration")
@Validated
@RestController
@RequestMapping("/api/v1/target")
public class DataParameterizationRest {

  @Resource
  private DataParameterizationFacade dataParameterizationFacade;

  @Operation(summary = "Preview the variables and datasets values of API, API_CASE, SCENARIO", operationId = "target:parameter:data:value")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{targetId}/{targetType}/parameter/data/value")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values: API, API_CASE, SCENARIO ", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(dataParameterizationFacade.valuePreview(targetId, targetType));
  }

}
