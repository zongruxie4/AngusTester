package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ScenarioFollowFacade {

  IdKey<Long, Object> add(Long scenarioId);

  void cancel(Long scenarioId);

  void cancelAll(Long projectId);

  PageResult<ScenarioFollowDetailVo> list(ScenarioFollowFindDto dto);

  Long count(Long projectId);

}
