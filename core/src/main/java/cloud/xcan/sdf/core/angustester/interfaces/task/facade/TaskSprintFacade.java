package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;

public interface TaskSprintFacade {

  IdKey<Long, Object> add(TaskSprintAddDto dto);

  void update(TaskSprintUpdateDto dto);

  IdKey<Long, Object> replace(TaskSprintReplaceDto dto);

  void start(Long id);

  void end(Long id);

  void block(Long id);

  IdKey<Long, Object> clone(Long id);

  void move(Long id, Long projectId);

  void restart(HashSet<Long> ids);

  void reopen(HashSet<Long> ids);

  void delete(Long id);

  TaskSprintDetailVo detail(Long id);

  PageResult<TaskSprintDetailVo> list(TaskSprintFindDto dto);

  PageResult<TaskSprintDetailVo> search(TaskSprintSearchDto dto);

}
