package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ScenarioAuthFacade {

  IdKey<Long, Object> add(Long scenarioId, ScenarioAuthAddDto dto);

  void delete(Long scenarioId);

  void replace(Long scenarioId, ScenarioAuthReplaceDto dto);

  void enabled(Long scenarioId, Boolean enabledFlag);

  Boolean status(Long scenarioId);

  List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean adminFlag);

  ScenarioAuthCurrentVo currentUserAuth(Long scenarioId, Boolean adminFlag);

  void authCheck(Long scenarioId, ScenarioPermission authPermission, Long userId);

  PageResult<ScenarioAuthVo> list(ScenarioAuthFindDto dto);

}
