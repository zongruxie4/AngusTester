package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import cloud.xcan.angus.core.tester.application.query.data.DataQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceCount;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisCommonFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.MockServiceCountDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ScriptStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisCommonAssembler;
import jakarta.annotation.Resource;
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
