package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ScenarioAuthQuery {

  Boolean status(Long scenarioId);

  ScenarioAuth checkAndFind(Long id);

  List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean admin);

  ScenarioAuthCurrent currentUserAuth(Long scenarioId, Boolean admin);

  void check(Long scenarioId, ScenarioPermission authPermission, Long userId);

  Page<ScenarioAuth> find(Specification<ScenarioAuth> spec,
      List<String> scenarioIds, Pageable pageable);

  void checkViewAuth(Long userId, Long scenarioId);

  void checkModifyAuth(Long userId, Long scenarioId);

  void checkDeleteAuth(Long userId, Long scenarioId);

  void checkTestAuth(Long userId, Long scenarioId);

  void checkGrantAuth(Long userId, Long scenarioId);

  void checkExportAuth(Long userId, Long scenarioId);

  void checkAuth(Long userId, Long scenarioId, ScenarioPermission permission);

  void checkAuth(Long userId, Long scenarioId, ScenarioPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> scenarioIds, ScenarioPermission permission);

  void checkRepeatAuth(Long scenarioId, Long authObjectId, AuthObjectType authObjectType);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, ScenarioPermission permission);

  List<ScenarioAuth> findAuth(Long userId, Long scenarioId);

  List<ScenarioAuth> findAuth(Long userId, Collection<Long> scenarioIds);

  List<ScenarioPermission> getUserAuth(Long scenarioId, Long userId);

  boolean isCreator(Long userId, Long scenarioId);

}




