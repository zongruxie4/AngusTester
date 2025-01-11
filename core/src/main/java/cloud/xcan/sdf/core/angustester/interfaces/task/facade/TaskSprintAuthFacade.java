package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface TaskSprintAuthFacade {

  IdKey<Long, Object> add(Long sprintId, TaskSprintAuthAddDto dto);

  void replace(Long sprintId, TaskSprintAuthReplaceDto dto);

  void enabled(Long sprintId, Boolean enabled);

  Boolean status(Long sprintId);

  void delete(Long sprintId);

  List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean adminFlag);

  TaskSprintAuthCurrentVo currentUserAuth(Long sprintId, Boolean adminFlag);

  void authCheck(Long sprintId, TaskSprintPermission authPermission, Long userId);

  PageResult<TaskSprintAuthVo> list(TaskSprintAuthFindDto dto);

}
