package cloud.xcan.angus.core.tester.interfaces.kanban;


import cloud.xcan.angus.core.tester.domain.data.DataResourcesCount;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsLabel;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsRanking;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesApisCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesFuncCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScenarioCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScriptCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesTaskCount;
import cloud.xcan.angus.core.tester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanDataAssetsFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Kanban - Data Assets", description = "Data Assets Analytics - Platform-wide business data growth trends and categorical statistics for resource utilization analysis and strategic planning")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/dataassets")
public class KanbanDataAssetsRest {

  @Resource
  private KanbanDataAssetsFacade kanbanDataAssetsFacade;

  @Operation(summary = "Query data assets growth trend analysis",
      description = "Retrieve growth trend data for various business assets with time series analysis for strategic planning",
      operationId = "kanban:dataassets:growthtrend")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Data assets growth trend retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Growth trend data not found for specified criteria")})
  @GetMapping(value = "/growthtrend")
  public ApiLocaleResult<Map<DataAssetsLabel, List<DataAssetsTimeSeries>>> growthTrend(
      @Valid @ParameterObject KanbanDataAssetsFindDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.growthTrend(dto));
  }

  @Operation(summary = "Functional testing resources statistics",
      description = "Generate statistics for functional testing resources including test cases, plans, and execution metrics",
      operationId = "kanban:dataassets:func:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional testing resources statistics generated successfully")})
  @GetMapping("/func")
  public ApiLocaleResult<ResourcesFuncCount> funcResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.funcResourcesStatistics(dto));
  }

  @Operation(summary = "API resources statistics",
      description = "Generate statistics for API resources including endpoints, services, and integration metrics",
      operationId = "kanban:dataassets:apis:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API resources statistics generated successfully")})
  @GetMapping("/apis")
  public ApiLocaleResult<ResourcesApisCount> apisResourcesStatistics(
      @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.apisResourcesStatistics(dto));
  }

  @Operation(summary = "Scenario resources statistics",
      description = "Generate statistics for test scenario resources including scenario definitions and execution metrics",
      operationId = "kanban:dataassets:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario resources statistics generated successfully")})
  @GetMapping("/scenarios")
  public ApiLocaleResult<ResourcesScenarioCount> scenarioResourcesStatistics(
      @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scenarioResourcesStatistics(dto));
  }

  @Operation(summary = "Task resources statistics",
      description = "Generate statistics for task resources including task assignments, progress tracking, and completion metrics",
      operationId = "kanban:dataassets:task:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task resources statistics generated successfully")})
  @GetMapping("/task")
  public ApiLocaleResult<ResourcesTaskCount> taskResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.taskResourcesStatistics(dto));
  }

  @Operation(summary = "Script resources statistics",
      description = "Generate statistics for script resources including automation scripts and execution metrics",
      operationId = "kanban:dataassets:script:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script resources statistics generated successfully")})
  @GetMapping("/script")
  public ApiLocaleResult<ResourcesScriptCount> scriptResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scriptResourcesStatistics(dto));
  }

  // Plan -> Do in funcResourcesStatistics() and taskResourcesStatistics() with main resources

  @Operation(summary = "Mock resources statistics",
      description = "Generate statistics for mock service resources including mock endpoints and usage metrics",
      operationId = "kanban:dataassets:mock:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock resources statistics generated successfully")})
  @GetMapping("/mock")
  public ApiLocaleResult<MockResourcesCreationCount> mockResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsMockCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.mockResourcesStatistics(dto));
  }

  @Operation(summary = "Data resources statistics",
      description = "Generate statistics for data resources including datasets, variables, and datasource metrics",
      operationId = "kanban:dataassets:data:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Data resources statistics generated successfully")})
  @GetMapping("/data")
  public ApiLocaleResult<DataResourcesCount> dataResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsDataCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.dataResourcesStatistics(dto));
  }

  @Operation(summary = "Query data asset contribution ranking",
      description = "Retrieve ranking data for data asset contributions with performance metrics and comparative analysis",
      operationId = "kanban:dataassets:ranking")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Data asset contribution ranking retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Ranking data not found for specified criteria")})
  @GetMapping(value = "/ranking")
  public ApiLocaleResult<DataAssetsRanking> ranking(
      @Valid @ParameterObject KanbanDataAssetsRankDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.ranking(dto));
  }

}
