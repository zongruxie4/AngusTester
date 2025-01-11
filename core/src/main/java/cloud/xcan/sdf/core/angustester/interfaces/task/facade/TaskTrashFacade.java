package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;

public interface TaskTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<TaskTrashDetailVo> search(TaskTrashSearchDto dto);

}
