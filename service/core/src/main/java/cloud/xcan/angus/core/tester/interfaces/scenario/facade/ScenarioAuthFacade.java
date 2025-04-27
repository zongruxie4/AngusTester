package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface ScenarioAuthFacade {

  IdKey<Long, Object> add(Long scenarioId, ScenarioAuthAddDto dto);

  void delete(Long scenarioId);

  void replace(Long scenarioId, ScenarioAuthReplaceDto dto);

  void enabled(Long scenarioId, Boolean enabled);

  Boolean status(Long scenarioId);

  List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean admin);

  ScenarioAuthCurrentVo currentUserAuth(Long scenarioId, Boolean admin);

  void authCheck(Long scenarioId, ScenarioPermission authPermission, Long userId);

  PageResult<ScenarioAuthVo> list(ScenarioAuthFindDto dto);

}
