package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade;

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
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
import java.util.List;
import java.util.Map;

public interface KanbanDataAssetsFacade {

  Map<DataAssetsLabel, List<DataAssetsTimeSeries>> growthTrend(KanbanDataAssetsFindDto dto);

  ResourcesFuncCount funcResourcesStatistics(KanbanDataAssetsCountDto dto);

  ResourcesApisCount apisResourcesStatistics(KanbanDataAssetsCountDto dto);

  ResourcesScenarioCount scenarioResourcesStatistics(KanbanDataAssetsCountDto dto);

  ResourcesTaskCount taskResourcesStatistics(KanbanDataAssetsCountDto dto);

  ResourcesScriptCount scriptResourcesStatistics(KanbanDataAssetsCountDto dto);

  MockResourcesCreationCount mockResourcesStatistics(KanbanDataAssetsMockCountDto dto);

  DataResourcesCount dataResourcesStatistics(KanbanDataAssetsDataCountDto dto);

  ReportResourcesCount reportResourcesStatistics(KanbanDataAssetsDataCountDto dto);

  DataAssetsRanking ranking(KanbanDataAssetsRankDto dto);

}
