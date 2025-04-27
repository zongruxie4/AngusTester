package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.core.tester.application.converter.TaskSprintConverter.getSprintCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_DATE_RANGE_ERROR_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_COMPLETED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_STARTED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_STARTED_CODE;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singleton;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.task.count.SprintTaskNum;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.infra.persistence.mysql.task.TaskRepoMysql;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.utils.DateUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TaskSprintQueryImpl implements TaskSprintQuery {

  @Resource
  private TaskSprintRepo taskSprintRepo;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private TaskMeetingQuery taskMeetingQuery;

  @Resource
  private TaskSprintAuthQuery taskSprintAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;
  @Autowired
  private TaskRepoMysql taskRepo;

  @Override
  public TaskSprint detail(Long id) {
    return new BizTemplate<TaskSprint>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected TaskSprint process() {
        TaskSprint sprint = taskSprintRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "Sprint"));

        List<TaskSprint> sprints = List.of(sprint);
        setTaskNum(sprints, singleton(id));
        setProgress(sprints, singleton(id));
        setMembers(sprints, singleton(id));

        sprint.setMeetings(taskMeetingQuery.findBySprintId(id));

        // Set user name and avatar
        userManager.setUserNameAndAvatar(sprints, "ownerId", "ownerName", "ownerAvatar");
        return sprint;
      }
    }.execute();
  }

  @Override
  public Page<TaskSprint> find(GenericSpecification<TaskSprint> spec, Pageable pageable) {
    return new BizTemplate<Page<TaskSprint>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<TaskSprint> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // checkAndSetAuthObjectIdCriteria(criteria); -> All project members are visible

        Page<TaskSprint> page = taskSprintRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          Set<Long> sprintIds = page.getContent().stream().map(TaskSprint::getId)
              .collect(Collectors.toSet());
          setTaskNum(page.getContent(), sprintIds);
          setProgress(page.getContent(), sprintIds);
          setMembers(page.getContent(), sprintIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public TaskSprint findLeastByProjectId(Long projectId) {
    return taskSprintRepo.findLeastByProjectId(projectId);
  }

  @Override
  public TaskSprint checkAndFind(Long id) {
    return taskSprintRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Sprint"));
  }

  @Override
  public List<TaskSprint> checkAndFind(Collection<Long> ids) {
    List<TaskSprint> sprints = taskSprintRepo.findAllById(ids);
    ProtocolAssert.assertResourceNotFound(isNotEmpty(sprints), ids.iterator().next(), "Sprint");
    if (ids.size() != sprints.size()) {
      for (TaskSprint sprint : sprints) {
        ProtocolAssert.assertResourceNotFound(ids.contains(sprint.getId()), sprint.getId(),
            "Sprint");
      }
    }
    return sprints;
  }

  @Override
  public boolean isAuthCtrl(Long id) {
    TaskSprint sprint = taskSprintRepo.findById(id).orElse(null);
    return nonNull(sprint) && sprint.getAuth();
  }

  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = taskSprintRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(TASK_SPRINT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  @Override
  public void checkSprintDateRange(LocalDateTime startDate, LocalDateTime deadlineDate) {
    if (startDate.isAfter(deadlineDate) || startDate.plusMinutes(10)
        .isBefore(LocalDateTime.now())) {
      throw ProtocolException.of(TASK_SPRINT_DATE_RANGE_ERROR_T,
          new Object[]{DateUtils.formatDate(DateUtils.asDate(startDate), DEFAULT_DATE_TIME_FORMAT),
              DateUtils.formatDate(DateUtils.asDate(deadlineDate), DEFAULT_DATE_TIME_FORMAT)});
    }
  }

  @Override
  public void checkHasStarted(TaskSprint sprint) {
    if (!sprint.getStatus().isInProcess()) {
      throw BizException.of(TASK_SPRINT_NOT_STARTED_CODE, TASK_SPRINT_NOT_STARTED);
    }
  }

  @Override
  public void checkSprintTasksCompleted(Long id) {
    long num = taskRepo.countNotCompletedBySprintId(id);
    if (num > 0) {
      throw ProtocolException.of(TASK_SPRINT_NOT_COMPLETED);
    }
  }

  @Override
  public int checkQuota() {
    long count = taskSprintRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterSprint, null, count + 1);
    return (int) count;
  }

  @Override
  public void setTaskNum(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      Map<Long, Long> taskNumsMap = taskInfoRepo.findSprintTaskNumsGroupBySprintId(sprintIds)
          .stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));
      for (TaskSprint sprint : sprints) {
        sprint.setTaskNum(taskNumsMap.containsKey(sprint.getId())
            ? taskNumsMap.get(sprint.getId()) : 0);
      }
      Map<Long, Long> validTaskNumsMap = taskInfoRepo.findValidSprintTaskNumsGroupBySprintId(
              sprintIds).stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));
      for (TaskSprint sprint : sprints) {
        sprint.setValidTaskNum(validTaskNumsMap.containsKey(sprint.getId())
            ? validTaskNumsMap.get(sprint.getId()) : 0);
      }
    }
  }

  /**
   * Note: Must be executed after setTaskNum().
   */
  @Override
  public void setProgress(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      Map<Long, Long> sprintPassedNumsMap = taskInfoRepo.findSprintPassedTaskNumsGroupBySprintId(
              sprintIds).stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));
      for (TaskSprint sprint : sprints) {
        if (sprintPassedNumsMap.containsKey(sprint.getId())) {
          sprint.setProgress(new Progress().setTotal(sprint.getValidTaskNum())
              .setCompleted(sprintPassedNumsMap.get(sprint.getId()))
              .setCompletedRate(sprint.getValidTaskNum() > 0 ?
                  BigDecimal.valueOf(sprintPassedNumsMap.get(sprint.getId()))
                      .divide(BigDecimal.valueOf(sprint.getValidTaskNum()), 4,
                          RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
                  : BigDecimal.ZERO));
        } else {
          sprint.setProgress(new Progress().setTotal(sprint.getValidTaskNum())
              .setCompleted(0).setCompletedRate(BigDecimal.ZERO));
        }
      }
    }
  }

  @Override
  public void setMembers(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      for (TaskSprint sprint : sprints) {
        List<UserInfo> members = new ArrayList<>();
        Set<Long> memberIds = taskQuery.getAssociateUsersBySprintId(sprint.getId());
        if (isNotEmpty(memberIds)) {
          Map<Long, UserBase> users = userManager.getUserBaseMap(memberIds);
          for (Long memberId : memberIds) {
            if (users.containsKey(memberId)) {
              members.add(CoreUtils.copyProperties(users.get(memberId), new UserInfo()));
            }
          }
        }
        sprint.setMembers(members);
      }
    }
  }

  @Override
  public void setSafeCloneName(TaskSprint sprint) {
    String saltName = randomAlphanumeric(3);
    String clonedName = taskSprintRepo.existsByProjectIdAndName(sprint.getProjectId(),
        sprint.getName() + "-Copy") ? sprint.getName() + "-Copy." + saltName
        : sprint.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    sprint.setName(clonedName);
  }

  /**
   * Set authorization conditions when you are not an administrator or only query yourself
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria) {
    SearchCriteria adminCriteria = findFirstAndRemove(criteria, "admin");
    boolean admin = false;
    if (Objects.nonNull(adminCriteria)) {
      admin = parseBoolean(adminCriteria.getValue().toString().replaceAll("\"", ""));
    }
    if (!admin || !taskSprintAuthQuery.isAdminUser()) {
      criteria.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }
    return false;
  }

  @Override
  public List<TaskSprint> getSprintCreatedSummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getSprintCreatorResourcesFilter(projectId, sprintId,
        createdDateStart, createdDateEnd, creatorIds);
    return taskSprintRepo.findAllByFilters(allFilters);
  }
}
