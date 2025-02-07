package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioListVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;

public interface ScenarioFacade {

  IdKey<Long, Object> add(ScenarioAddDto dto);

  void update(ScenarioUpdateDto dto);

  IdKey<Long, Object> replace(ScenarioReplaceDto dto);

  void move(Long scenarioId, Long targetProjectId);

  IdKey<Long, Object> clone(Long id);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  void delete(Long id);

  ScenarioDetailVo detail(Long id);

  List<ScenarioListVo> list(Set<Long> ids);

  PageResult<ScenarioListVo> list(ScenarioInfoFindDto dto);

  PageResult<ScenarioListVo> search(ScenarioInfoSearchDto dto);

}
