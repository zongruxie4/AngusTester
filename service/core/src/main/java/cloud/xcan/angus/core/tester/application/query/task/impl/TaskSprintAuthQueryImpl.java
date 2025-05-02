package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NO_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthCurrent;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TaskSprintAuthQueryImpl implements TaskSprintAuthQuery {

  @Resource
  private TaskSprintAuthRepo taskSprintAuthRepo;

  @Resource
  private TaskSprintRepo taskSprintRepo;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private UserRepo userRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Boolean status(Long sprintId) {
    return new BizTemplate<Boolean>() {
      TaskSprint taskSprintDb;

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        taskSprintDb = taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected Boolean process() {
        return taskSprintDb.getAuth();
      }
    }.execute();
  }

  @Override
  public List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean admin) {
    return new BizTemplate<List<TaskSprintPermission>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        sprintDb = taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected List<TaskSprintPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return TaskSprintPermission.ALL;
        }

        List<TaskSprintAuth> sprintAuths = findAuth(userId, sprintId);
        if (isCreator(sprintAuths)) {
          return TaskSprintPermission.ALL;
        }

        return sprintAuths.stream().map(TaskSprintAuth::getAuths)
            .flatMap(Collection::stream).distinct().collect(Collectors.toList());
      }
    }.execute();
  }

  @Override
  public TaskSprintAuthCurrent currentUserAuth(Long sprintId, Boolean admin) {
    return new BizTemplate<TaskSprintAuthCurrent>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        sprintDb = taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected TaskSprintAuthCurrent process() {
        TaskSprintAuthCurrent authCurrent = new TaskSprintAuthCurrent();
        authCurrent.setTaskSprintAuth(sprintDb.getAuth());

        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(TaskSprintPermission.ALL);
          return authCurrent;
        }

        List<TaskSprintAuth> sprintAuths = findAuth(getUserId(), sprintId);
        if (isCreator(sprintAuths)) {
          authCurrent.addPermissions(TaskSprintPermission.ALL);
          return authCurrent;
        }

        authCurrent.addPermissions(sprintAuths.stream()
            .map(TaskSprintAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet()));
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public void check(Long sprintId, TaskSprintPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, sprintId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<TaskSprintAuth> find(GenericSpecification<TaskSprintAuth> spec, List<String> sprintIds,
      Pageable pageable) {
    return new BizTemplate<Page<TaskSprintAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(sprintIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"sprintId"});
      }

      @Override
      protected Page<TaskSprintAuth> process() {
        return taskSprintAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public TaskSprintAuth checkAndFind(Long id) {
    return taskSprintAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TaskSprintAuth"));
  }

  @Override
  public void checkModifySprintAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.MODIFY_SPRINT);
  }

  @Override
  public void checkDeleteSprintAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.DELETE_SPRINT);
  }

  @Override
  public void checkAddTaskAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.ADD_TASK);
  }

  @Override
  public void checkModifyTaskAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.MODIFY_TASK);
  }

  @Override
  public void checkGrantAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.GRANT, false, true);
  }

  @Override
  public void checkAuth(Long userId, Long sprintId, TaskSprintPermission permission) {
    checkAuth(userId, sprintId, permission, false, permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, @Nullable Long sprintId, TaskSprintPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (isNull(sprintId) /* Fix: Backlog or general project management sprint is null */
        || !ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.notIgnorePublicAccess()
        && !taskSprintQuery.isAuthCtrl(sprintId)) {
      return;
    }

    List<TaskSprintAuth> projectAuths = findAuth(userId, sprintId);
    if (isCreator(projectAuths)) {
      return;
    }
    BizAssert.assertTrue(flatPermissions(projectAuths).contains(permission),
        TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH, new Object[]{permission});
  }

  /**
   * Verify the operation permissions of the sprint
   */
  @Override
  public void batchCheckPermission(Collection<Long> sprintIds, TaskSprintPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(sprintIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? sprintIds : taskSprintRepo.findIds0ByIdInAndAuth(sprintIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<TaskSprintAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      TaskSprint sprint = taskSprintRepo.findById(firstId).orElse(null);
      BizAssert.assertTrue(false, TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH,
          new Object[]{permission, Objects.isNull(sprint) ? firstId : sprint.getName()});
    }

    Map<Long, List<TaskSprintAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getSprintId()))
        .collect(Collectors.groupingBy(TaskSprintAuth::getSprintId));
    for (Long sprintId : authMap.keySet()) {
      List<TaskSprintAuth> values = authMap.get(sprintId);
      if (isNotEmpty(values)) {
        List<TaskSprintPermission> projectPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(projectPermissions) && projectPermissions.contains(permission)) {
          continue;
        }
      }
      TaskSprint sprint = taskSprintRepo.findById(sprintId).orElse(null);
      BizAssert.assertTrue(false, TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH,
          new Object[]{permission, Objects.isNull(sprint) ? sprintId : sprint.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long sprintId, Long authObjectId, AuthObjectType authObjectType) {
    if (taskSprintAuthRepo.countBySprintIdAndAuthObjectIdAndAuthObjectType(sprintId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId,
      TaskSprintPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return taskSprintAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(TaskSprintAuth::getSprintId)
        .collect(Collectors.toList());
  }

  @Override
  public List<TaskSprintAuth> findAuth(Long userId, Long sprintId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return taskSprintAuthRepo.findAllBySprintIdAndAuthObjectIdIn(sprintId, orgIds);
  }

  @Override
  public List<TaskSprintAuth> findAuth(Long userId, Collection<Long> projectIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(projectIds) ? taskSprintAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : taskSprintAuthRepo.findAllBySprintIdInAndAuthObjectIdIn(projectIds, orgIds);
  }

  @Override
  public List<TaskSprintPermission> getUserAuth(Long sprintId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return TaskSprintPermission.ALL;
    }

    List<TaskSprintAuth> auths = findAuth(userId, sprintId);
    if (isEmpty(auths)) {
      return null;
    }
    if (isCreator(auths)) {
      return TaskSprintPermission.ALL;
    }
    return auths.stream().map(TaskSprintAuth::getAuths)
        .flatMap(Collection::stream).distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isCreator(Long userId, Long sprintId) {
    return isCreator(findAuth(userId, sprintId));
  }

  @Override
  public boolean isAdminUser() {
    return commonQuery.isAdminUser();
  }

  private boolean isCreator(List<TaskSprintAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (TaskSprintAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  private Set<TaskSprintPermission> flatPermissions(List<TaskSprintAuth> auths) {
    Set<TaskSprintPermission> actions = new HashSet<>();
    for (TaskSprintAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}
