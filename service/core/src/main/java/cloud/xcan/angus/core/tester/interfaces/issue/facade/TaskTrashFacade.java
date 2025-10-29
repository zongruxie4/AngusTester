package cloud.xcan.angus.core.tester.interfaces.issue.facade;

import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.trash.TaskTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

public interface TaskTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<TaskTrashDetailVo> list(TaskTrashListDto dto);

}
