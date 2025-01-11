package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.trash.FuncTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.trash.FuncTrashDetailVo;

public interface FuncTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<FuncTrashDetailVo> search(FuncTrashSearchDto dto);

}
