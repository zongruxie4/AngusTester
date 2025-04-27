package cloud.xcan.angus.core.tester.interfaces.analysis.facade;


import cloud.xcan.angus.core.tester.domain.data.DataResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceCount;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.MockServiceCountDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ScriptStatisticsDto;

public interface AnalysisCommonFacade {

  ScenarioResourcesCreationCount scenarioResourcesStatistics(ResourcesStatisticsDto dto);

  TestScenarioCount testResult(ResourcesStatisticsDto dto);

  ScriptCount scriptCountStatistics(ScriptStatisticsDto dto);

  ScriptResourcesCreationCount scriptResourcesStatistics(ResourcesStatisticsDto dto);

  MockServiceCount mockServiceCountStatistics(MockServiceCountDto dto);

  DataResourcesCreationCount dataResourcesStatistics(ResourcesStatisticsDto dto);

}
