package cloud.xcan.angus.core.tester.interfaces.scenario.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.trash.ScenarioTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

public interface ScenarioTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<ScenarioTrashDetailVo> list(ScenarioTrashListDto dto);

}
