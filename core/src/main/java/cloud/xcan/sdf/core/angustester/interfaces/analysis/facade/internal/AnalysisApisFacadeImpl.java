package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisTestQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestApisCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ResourcesStatisticsDto;
import javax.annotation.Resource;
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
