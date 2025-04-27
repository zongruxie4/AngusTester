package cloud.xcan.angus.core.tester.interfaces.analysis.facade;


import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;

public interface AnalysisApisFacade {

  ApisResourcesCreationCount apisResourcesStatistics(ResourcesStatisticsDto dto);

  TestApisCount testResult(ResourcesStatisticsDto dto);

}
