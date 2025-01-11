package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisCommonFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.MockServiceCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ScriptStatisticsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "AnalysisCommon")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisCommonRest {

  @Resource
  private AnalysisCommonFacade analysisAngusFacade;

  @ApiOperation(value = "Scenario creation resources count statistics", nickname = "analysis:scenario:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/scenario/resources/count")
  public ApiLocaleResult<ScenarioResourcesCreationCount> scenarioResourcesStatistics(
      ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.scenarioResourcesStatistics(dto));
  }

  @ApiOperation(value = "Query the test results of scenario", nickname = "analysis:scenario:test:result:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/scenario/test/result/count")
  public ApiLocaleResult<TestScenarioCount> testResult(ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.testResult(dto));
  }

  @ApiOperation(value = "Script creation resources count statistics", nickname = "analysis:script:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/script/resources/count")
  public ApiLocaleResult<ScriptResourcesCreationCount> scriptResourcesStatistics(
      ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.scriptResourcesStatistics(dto));
  }

  @ApiOperation(value = "Script resources count statistics", nickname = "analysis:script:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/script/count")
  public ApiLocaleResult<ScriptCount> scriptCountStatistics(ScriptStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.scriptCountStatistics(dto));
  }

  @ApiOperation(value = "Mock service resources and mock status count statistics", nickname = "analysis:mock:service:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/mock/service/count")
  public ApiLocaleResult<MockServiceCount> mockServiceCount(@Valid MockServiceCountDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.mockServiceCountStatistics(dto));
  }

  @ApiOperation(value = "Data creation resources statistics", nickname = "analysis:data:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/data/resources")
  public ApiLocaleResult<DataResourcesCreationCount> dataResourcesStatistics(
      ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFacade.dataResourcesStatistics(dto));
  }

}
