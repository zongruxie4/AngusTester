package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.trash.ScenarioTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;

public interface ScenarioTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<ScenarioTrashDetailVo> search(ScenarioTrashSearchDto dto);

}
