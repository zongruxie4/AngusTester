package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisApisFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AnalysisApisFacadeImpl implements AnalysisApisFacade {

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisTestQuery apisTestQuery;

  @Override
  public ApisResourcesCreationCount apisResourcesStatistics(ResourcesStatisticsDto dto) {
    return apisQuery.creationStatistics(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public TestApisCount testResult(ResourcesStatisticsDto dto) {
    return apisTestQuery.countTestResult(dto.getProjectId(), dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

}
