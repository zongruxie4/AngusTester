package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.services.ApisTestCount;
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
