package cloud.xcan.angus.core.tester.interfaces.kanban.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.kanban.facade.internal.assembler.KanbanDataAssetsAssembler.toCount;

import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.data.DataQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.kanban.KanbanDataAssetsQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsLabel;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsRanking;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesApisCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesFuncCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScenarioCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScriptCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesTaskCount;
import cloud.xcan.angus.core.tester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanDataAssetsFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KanbanDataAssetsFacadeImpl implements KanbanDataAssetsFacade {

  @Resource
  private KanbanDataAssetsQuery kanbanDataAssetsQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private DataQuery dataQuery;

  @Override
  public Map<DataAssetsLabel, List<DataAssetsTimeSeries>> growthTrend(
      KanbanDataAssetsFindDto dto) {
    return kanbanDataAssetsQuery.growthTrend(dto.getCreatorObjectType(), dto.getCreatorObjectId(),
        dto.getProjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd(), dto.getCategory());
  }

  @Override
  public ResourcesFuncCount funcResourcesStatistics(KanbanDataAssetsCountDto dto) {
    FuncLastResourceCreationCount count = funcCaseQuery.creationResourcesStatistics(dto.getProjectId(),
        null, dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), true, true, true);
    return toCount(count);
  }

  @Override
  public ResourcesApisCount apisResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ApisResourcesCreationCount count = apisQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return toCount(count);
  }

  @Override
  public ResourcesScenarioCount scenarioResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ScenarioResourcesCreationCount count = scenarioQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return toCount(count);
  }

  @Override
  public ResourcesTaskCount taskResourcesStatistics(KanbanDataAssetsCountDto dto) {
    TaskLastResourceCreationCount count = taskQuery.creationResourcesStatistics(dto.getProjectId(),
        null, dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), true, true);
    return toCount(count);
  }

  @Override
  public ResourcesScriptCount scriptResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ScriptResourcesCreationCount count = scriptQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return toCount(count);
  }

  @Override
  public MockResourcesCreationCount mockResourcesStatistics(KanbanDataAssetsMockCountDto dto) {
    return mockServiceQuery.creationStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public DataResourcesCount dataResourcesStatistics(KanbanDataAssetsDataCountDto dto) {
    return dataQuery.countStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public DataAssetsRanking ranking(KanbanDataAssetsRankDto dto) {
    return kanbanDataAssetsQuery.ranking(dto.getCreatorObjectType(), dto.getCreatorObjectId(),
        dto.getProjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }
}
