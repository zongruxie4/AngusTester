package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.issue.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.core.tester.domain.issue.sprint.auth.TaskSprintAuthCurrent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskSprintAuthQuery {

  Boolean status(Long sprintId);

  List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean admin);

  TaskSprintAuthCurrent currentUserAuth(Long sprintId, Boolean admin);

  void check(Long sprintId, TaskSprintPermission authPermission, Long userId);

  Page<TaskSprintAuth> find(GenericSpecification<TaskSprintAuth> spec, List<String> sprintIds,
      Pageable pageable);

  TaskSprintAuth checkAndFind(Long id);

  void checkModifySprintAuth(Long userId, Long sprintId);

  void checkDeleteSprintAuth(Long userId, Long sprintId);

  void checkAddTaskAuth(Long userId, Long sprintId);

  void checkModifyTaskAuth(Long userId, Long sprintId);

  void checkGrantAuth(Long userId, Long sprintId);

  void checkAuth(Long userId, Long sprintId, TaskSprintPermission permission);

  void checkAuth(Long userId, Long sprintId, TaskSprintPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> sprintIds, TaskSprintPermission permission);

  void checkRepeatAuth(Long sprintId, Long authObjectId, AuthObjectType authObjectType);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, TaskSprintPermission permission);

  List<TaskSprintAuth> findAuth(Long userId, Long sprintId);

  List<TaskSprintAuth> findAuth(Long userId, Collection<Long> sprintIds);

  List<TaskSprintPermission> getUserAuth(Long sprintId, Long userId);

  boolean isCreator(Long userId, Long sprintId);

  boolean isAdminUser();

}
