package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade;


import cloud.xcan.sdf.core.angustester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestApisCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;

public interface AnalysisApisFacade {

  ApisResourcesCreationCount apisResourcesStatistics(ResourcesStatisticsDto dto);

  TestApisCount testResult(ResourcesStatisticsDto dto);

}
