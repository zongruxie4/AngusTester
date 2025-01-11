package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.DataQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.kanban.KanbanDataAssetsQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncLastResourceCreationCount;
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
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.KanbanDataAssetsFacade;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsDataCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsMockCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanDataAssetsRankDto;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.internal.assembler.KanbanDataAssetsAssembler;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
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

  @Resource
  private ReportQuery reportQuery;

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
    return KanbanDataAssetsAssembler.toCount(count);
  }

  @Override
  public ResourcesApisCount apisResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ApisResourcesCreationCount count = apisQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return KanbanDataAssetsAssembler.toCount(count);
  }

  @Override
  public ResourcesScenarioCount scenarioResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ScenarioResourcesCreationCount count = scenarioQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return KanbanDataAssetsAssembler.toCount(count);
  }

  @Override
  public ResourcesTaskCount taskResourcesStatistics(KanbanDataAssetsCountDto dto) {
    TaskLastResourceCreationCount count = taskQuery.creationResourcesStatistics(dto.getProjectId(),
        null, dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), true, true);
    return KanbanDataAssetsAssembler.toCount(count);
  }

  @Override
  public ResourcesScriptCount scriptResourcesStatistics(KanbanDataAssetsCountDto dto) {
    ScriptResourcesCreationCount count = scriptQuery.creationStatistics(dto.getProjectId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd());
    return KanbanDataAssetsAssembler.toCount(count);
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
  public ReportResourcesCount reportResourcesStatistics(KanbanDataAssetsDataCountDto dto) {
    return reportQuery.countStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public DataAssetsRanking ranking(KanbanDataAssetsRankDto dto) {
    return kanbanDataAssetsQuery.ranking(dto.getCreatorObjectType(), dto.getCreatorObjectId(),
        dto.getProjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }
}
