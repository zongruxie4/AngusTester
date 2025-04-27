package cloud.xcan.angus.core.tester.interfaces.kanban.facade;

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
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
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

  DataAssetsRanking ranking(KanbanDataAssetsRankDto dto);

}
