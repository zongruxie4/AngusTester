package cloud.xcan.sdf.api.angustester.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ScenarioTestDoorRemote {

  @ApiOperation(value = "Find enabled functionality, performance, stability testing type of scenario.", nickname = "scenario:test:enabled:find:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/doorapi/v1/scenario/{id}/test/enabled")
  ApiLocaleResult<List<TestType>> testEnabledFind(@ApiParam(name = "id", required = true) @PathVariable("id") Long scenarioId);

  @ApiOperation(value = "The testing scenario summary the functionality, performance, stability testing of project", nickname = "project:test:scenario:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/doorapi/v1/project/{id}/test/scenario/count")
  ApiLocaleResult<ScenarioTestCount> countProjectTestScenarios(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      @SpringQueryMap OrgAndDateFilterDto dto);

}
