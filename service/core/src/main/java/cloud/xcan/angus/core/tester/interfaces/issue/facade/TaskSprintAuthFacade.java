package cloud.xcan.angus.core.tester.interfaces.issue.facade;

import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.auth.TaskSprintAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface TaskSprintAuthFacade {

  IdKey<Long, Object> add(Long sprintId, TaskSprintAuthAddDto dto);

  void replace(Long sprintId, TaskSprintAuthReplaceDto dto);

  void enabled(Long sprintId, Boolean enabled);

  Boolean status(Long sprintId);

  void delete(Long sprintId);

  List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean admin);

  TaskSprintAuthCurrentVo currentUserAuth(Long sprintId, Boolean admin);

  void authCheck(Long sprintId, TaskSprintPermission authPermission, Long userId);

  PageResult<TaskSprintAuthVo> list(TaskSprintAuthFindDto dto);

}
