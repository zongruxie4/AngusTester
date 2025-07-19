package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;

public interface ScenarioFacade {

  IdKey<Long, Object> add(ScenarioAddDto dto);

  void update(ScenarioUpdateDto dto);

  IdKey<Long, Object> replace(ScenarioReplaceDto dto);

  void move(Long scenarioId, Long targetProjectId);

  IdKey<Long, Object> clone(Long id);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Long id);

  ScenarioDetailVo detail(Long id);

  List<ScenarioListVo> list(Set<Long> ids);

  PageResult<ScenarioListVo> list(ScenarioInfoFindDto dto);

}
