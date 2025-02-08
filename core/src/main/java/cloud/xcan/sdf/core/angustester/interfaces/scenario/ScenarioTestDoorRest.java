package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioTestFacade;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ScenarioTestDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1")
public class ScenarioTestDoorRest {

  @Resource
  private ScenarioTestFacade scenarioTestFacade;

  @ApiOperation(value = "Find enabled functionality, performance, stability testing type of scenario.", nickname = "scenario:test:enabled:find:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/scenario/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long scenario) {
    return ApiLocaleResult.success(scenarioTestFacade.testEnabledFind(scenario));
  }

  @ApiOperation(value = "The testing scenario summary the functionality, performance, stability testing of project", nickname = "project:test:scenario:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/scenario/count")
  public ApiLocaleResult<ScenarioTestCount> countProjectTestScenarios(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(scenarioTestFacade.countProjectTestScenarios(projectId, dto));
  }

}
