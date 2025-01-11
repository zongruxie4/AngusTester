package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ScenarioFollowFacade {

  IdKey<Long, Object> add(Long scenarioId);

  void cancel(Long scenarioId);

  void cancelAll(Long projectId);

  PageResult<ScenarioFollowDetailVo> search(ScenarioFollowFindDto dto);

  Long count(Long projectId);

}
