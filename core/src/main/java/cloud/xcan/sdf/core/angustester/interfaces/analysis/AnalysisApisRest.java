package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestApisCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "AnalysisApis")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisApisRest {

  @javax.annotation.Resource
  private AnalysisApisFacade analysisAngusApisFacade;

  @ApiOperation(value = "Apis and services creation statistics", nickname = "analysis:apis:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/apis/resources")
  public ApiLocaleResult<ApisResourcesCreationCount> apisResourcesStatistics(
      ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.apisResourcesStatistics(dto));
  }

  @ApiOperation(value = "Query the test results of apis", nickname = "analysis:apis:test:result:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/apis/test/result/count")
  public ApiLocaleResult<TestApisCount> testResult(ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.testResult(dto));
  }

}
