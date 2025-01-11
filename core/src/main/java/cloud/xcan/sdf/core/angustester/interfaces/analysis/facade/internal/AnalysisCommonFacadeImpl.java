package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.data.DataQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisCommonFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.MockServiceCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ScriptStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler.AnalysisCommonAssembler;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCommonFacadeImpl implements AnalysisCommonFacade {

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioTestQuery scenarioTestQuery;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private DataQuery dataQuery;

  @Override
  public ScenarioResourcesCreationCount scenarioResourcesStatistics(ResourcesStatisticsDto dto) {
    return scenarioQuery.creationStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public TestScenarioCount testResult(ResourcesStatisticsDto dto) {
    return scenarioTestQuery.countTestResult(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ScriptCount scriptCountStatistics(ScriptStatisticsDto dto) {
    return scriptQuery.countStatistics(AnalysisCommonAssembler.getSearchCriteria(dto));
  }

  @Override
  public ScriptResourcesCreationCount scriptResourcesStatistics(ResourcesStatisticsDto dto) {
    return scriptQuery.creationStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public MockServiceCount mockServiceCountStatistics(MockServiceCountDto dto) {
    return mockServiceQuery.countStatistics(dto.getMockServiceId());
  }

  @Override
  public DataResourcesCreationCount dataResourcesStatistics(ResourcesStatisticsDto dto) {
    return dataQuery.creationStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

}
