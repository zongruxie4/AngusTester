package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestApisCount;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.services.ApisTestCount;
import java.time.LocalDateTime;
import java.util.List;

public interface ApisTestQuery {

  List<TestType> findEnabledTestTypes(Long apisId);

  ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  TestApisCount countTestResult(Long projectId, AuthObjectType creatorObjectType, Long creatorObjectId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

}
