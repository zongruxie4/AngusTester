package cloud.xcan.angus.core.tester.interfaces.analysis;


import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisApisFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis APIs", description = "API Resource Analytics - Monitoring and statistical analysis of API resource creation, status tracking, and testing result aggregation")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisApisRest {

  @Resource
  private AnalysisApisFacade analysisAngusApisFacade;

  @Operation(summary = "API and service creation statistics", 
      description = "Retrieve statistics on API and service resource creation patterns and trends",
      operationId = "analysis:apis:resources:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API creation statistics retrieved successfully")})
  @GetMapping("/apis/resources")
  public ApiLocaleResult<ApisResourcesCreationCount> apisResourcesStatistics(
      @Valid @ParameterObject ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.apisResourcesStatistics(dto));
  }

  @Operation(summary = "API testing result statistics", 
      description = "Query and analyze API testing results with metrics and performance indicators",
      operationId = "analysis:apis:test:result:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API testing result statistics retrieved successfully")})
  @GetMapping(value = "/apis/test/result/count")
  public ApiLocaleResult<TestApisCount> testResult(
      @Valid @ParameterObject ResourcesStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusApisFacade.testResult(dto));
  }

}
