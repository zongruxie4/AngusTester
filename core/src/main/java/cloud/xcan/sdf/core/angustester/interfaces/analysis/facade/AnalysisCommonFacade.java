package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade;


import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.MockServiceCountDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ScriptStatisticsDto;

public interface AnalysisCommonFacade {

  ScenarioResourcesCreationCount scenarioResourcesStatistics(ResourcesStatisticsDto dto);

  TestScenarioCount testResult(ResourcesStatisticsDto dto);

  ScriptCount scriptCountStatistics(ScriptStatisticsDto dto);

  ScriptResourcesCreationCount scriptResourcesStatistics(ResourcesStatisticsDto dto);

  MockServiceCount mockServiceCountStatistics(MockServiceCountDto dto);

  DataResourcesCreationCount dataResourcesStatistics(ResourcesStatisticsDto dto);

}
