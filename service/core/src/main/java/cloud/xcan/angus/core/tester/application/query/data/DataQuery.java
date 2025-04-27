package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCount;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCreationCount;
import java.time.LocalDateTime;

public interface DataQuery {

  DataResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  DataResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

}
