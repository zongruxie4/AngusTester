package cloud.xcan.sdf.core.angustester.interfaces.kanban;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsLabel;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsRanking;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesApisCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesFuncCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesScriptCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesTaskCount;
import cloud.xcan.sdf.core.angustester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.report.ReportResourcesCount;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.KanbanDataAssetsFacade;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "KanbanDataAssets")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/dataassets")
public class KanbanDataAssetsRest {

  @Resource
  private KanbanDataAssetsFacade kanbanDataAssetsFacade;

  @ApiOperation(value = "Query the data assets growth trend", nickname = "kanban:dataassets:growthtrend")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/growthtrend")
  public ApiLocaleResult<Map<DataAssetsLabel, List<DataAssetsTimeSeries>>> growthTrend(
      @Valid KanbanDataAssetsFindDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.growthTrend(dto));
  }

  @ApiOperation(value = "Cases and associated plan, tag, and module resource statistics", nickname = "kanban:dataassets:func:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/func")
  public ApiLocaleResult<ResourcesFuncCount> funcResourcesStatistics(
      @Valid KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.funcResourcesStatistics(dto));
  }

  @ApiOperation(value = "Apis and services resource statistics", nickname = "kanban:dataassets:apis:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/apis")
  public ApiLocaleResult<ResourcesApisCount> apisResourcesStatistics(
      KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.apisResourcesStatistics(dto));
  }

  @ApiOperation(value = "Scenario resource statistics", nickname = "kanban:dataassets:scenario:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/scenarios")
  public ApiLocaleResult<ResourcesScenarioCount> scenarioResourcesStatistics(
      KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scenarioResourcesStatistics(dto));
  }

  @ApiOperation(value = "Task and associated module, plan and tag resources statistics", nickname = "kanban:dataassets:task:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/task")
  public ApiLocaleResult<ResourcesTaskCount> taskResourcesStatistics(
      @Valid KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.taskResourcesStatistics(dto));
  }

  @ApiOperation(value = "Task and associated module, plan and tag resources statistics", nickname = "kanban:dataassets:script:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/script")
  public ApiLocaleResult<ResourcesScriptCount> scriptResourcesStatistics(
      @Valid KanbanDataAssetsCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.scriptResourcesStatistics(dto));
  }

  // Plan -> Do in funcResourcesStatistics() and taskResourcesStatistics() with main resources

  @ApiOperation(value = "Mock resources statistics", nickname = "kanban:dataassets:mock:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/mock")
  public ApiLocaleResult<MockResourcesCreationCount> mockResourcesStatistics(
      @Valid KanbanDataAssetsMockCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.mockResourcesStatistics(dto));
  }

  @ApiOperation(value = "Data resources statistics", nickname = "kanban:dataassets:data:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/data")
  public ApiLocaleResult<DataResourcesCount> dataResourcesStatistics(
      @Valid KanbanDataAssetsDataCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.dataResourcesStatistics(dto));
  }

  @ApiOperation(value = "Report resources statistics", nickname = "kanban:dataassets:report:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/report")
  public ApiLocaleResult<ReportResourcesCount> reportResourcesStatistics(
      @Valid KanbanDataAssetsDataCountDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.reportResourcesStatistics(dto));
  }
  
  @ApiOperation(value = "Query the data assets ranking", nickname = "kanban:dataassets:growthtrend")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/ranking")
  public ApiLocaleResult<DataAssetsRanking> ranking(@Valid KanbanDataAssetsRankDto dto) {
    return ApiLocaleResult.success(kanbanDataAssetsFacade.ranking(dto));
  }

}
