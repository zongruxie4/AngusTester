package cloud.xcan.angus.core.tester.interfaces.analysis;


import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisApisFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "AnalysisApis")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisApisRest {

  @Resource
  private AnalysisApisFacade analysisAngusApisFacade;

  @Operation(description = "Apis and services creation statistics", operationId = "analysis:apis:resources:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/apis/resources")
  public ApiLocaleResult<ApisResourcesCreationCount> apisResourcesStatistics(
      ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.apisResourcesStatistics(dto));
  }

  @Operation(description = "Query the test results of apis", operationId = "analysis:apis:test:result:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/apis/test/result/count")
  public ApiLocaleResult<TestApisCount> testResult(ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.testResult(dto));
  }

}
