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

@Tag(name = "Kanban - DataAssets", description = "Business Data Growth Analytics - Query platform-wide business data growth trends and categorical statistics")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/dataassets")
public class KanbanDataAssetsRest {

  @Resource
  private KanbanDataAssetsFacade kanbanDataAssetsFacade;

  @Operation(summary = "Query the data assets growth trend", operationId = "kanban:dataassets:growthtrend")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/growthtrend")
  public ApiLocaleResult<Map<DataAssetsLabel, List<DataAssetsTimeSeries>>> growthTrend(
      @Valid @ParameterObject KanbanDataAssetsFindDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.growthTrend(dto));
  }

  @Operation(summary = "Functional testing resources statistics", operationId = "kanban:dataassets:func:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/func")
  public ApiLocaleResult<ResourcesFuncCount> funcResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.funcResourcesStatistics(dto));
  }

  @Operation(summary = "Api resources statistics", operationId = "kanban:dataassets:apis:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/apis")
  public ApiLocaleResult<ResourcesApisCount> apisResourcesStatistics(
      @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.apisResourcesStatistics(dto));
  }

  @Operation(summary = "Scenario resources statistics", operationId = "kanban:dataassets:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/scenarios")
  public ApiLocaleResult<ResourcesScenarioCount> scenarioResourcesStatistics(
      @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scenarioResourcesStatistics(dto));
  }

  @Operation(summary = "Task resources statistics", operationId = "kanban:dataassets:task:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/task")
  public ApiLocaleResult<ResourcesTaskCount> taskResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.taskResourcesStatistics(dto));
  }

  @Operation(summary = "Script resources statistics", operationId = "kanban:dataassets:script:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/script")
  public ApiLocaleResult<ResourcesScriptCount> scriptResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scriptResourcesStatistics(dto));
  }

  // Plan -> Do in funcResourcesStatistics() and taskResourcesStatistics() with main resources

  @Operation(summary = "Mock resources statistics", operationId = "kanban:dataassets:mock:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/mock")
  public ApiLocaleResult<MockResourcesCreationCount> mockResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsMockCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.mockResourcesStatistics(dto));
  }

  @Operation(summary = "Data resources statistics", operationId = "kanban:dataassets:data:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/data")
  public ApiLocaleResult<DataResourcesCount> dataResourcesStatistics(
      @Valid @ParameterObject KanbanDataAssetsDataCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.dataResourcesStatistics(dto));
  }

  @Operation(summary = "Query data asset contribution ranking", operationId = "kanban:dataassets:ranking")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/ranking")
  public ApiLocaleResult<DataAssetsRanking> ranking(
      @Valid @ParameterObject KanbanDataAssetsRankDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.ranking(dto));
  }

}
