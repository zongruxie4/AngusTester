package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCount;
import cloud.xcan.sdf.core.angustester.domain.data.DataResourcesCreationCount;
import java.time.LocalDateTime;

public interface DataQuery {

  DataResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  DataResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

}
