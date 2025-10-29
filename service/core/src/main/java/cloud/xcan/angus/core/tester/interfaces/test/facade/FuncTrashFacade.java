package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.trash.FuncTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.trash.FuncTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

public interface FuncTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<FuncTrashDetailVo> list(FuncTrashFindDto dto);

}
