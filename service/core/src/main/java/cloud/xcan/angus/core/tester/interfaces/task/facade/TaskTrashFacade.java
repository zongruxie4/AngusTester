package cloud.xcan.angus.core.tester.interfaces.task.facade;

import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

public interface TaskTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<TaskTrashDetailVo> search(TaskTrashSearchDto dto);

}
