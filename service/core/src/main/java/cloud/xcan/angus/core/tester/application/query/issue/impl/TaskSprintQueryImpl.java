package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.core.tester.application.converter.TaskSprintConverter.getSprintCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_DATE_RANGE_ERROR_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_COMPLETED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_STARTED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NOT_STARTED_CODE;
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
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.count.SprintTaskNum;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintSearchRepo;
import cloud.xcan.angus.core.tester.infra.persistence.mysql.master.issue.TaskRepoMysql;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.DateUtils;
import jakarta.annotation.Resource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskSprintQuery for task sprint management and query operations.
 * </p>
 * <p>
 * Provides comprehensive sprint management services including CRUD operations, progress tracking,
 * member management, quota validation, and authorization controls. Supports full-text search,
 * pagination, and complex filtering with proper business logic validation.
 * </p>
 */
@Biz
public class TaskSprintQueryImpl implements TaskSprintQuery {

  @Resource
  private TaskSprintRepo taskSprintRepo;
  @Resource
  private TaskSprintSearchRepo taskSprintSearchRepo;
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

  /**
   * <p>
   * Get detailed information of a task sprint by ID.
   * </p>
   * <p>
   * Retrieves sprint details and assembles all related information including task counts,
   * progress metrics, member information, and associated meetings. Sets user names and avatars.
   * </p>
   * @param id Sprint ID
   * @return Complete sprint details with all associated information
   */
  @Override
  public TaskSprint detail(Long id) {
    return new BizTemplate<TaskSprint>() {

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

  /**
   * <p>
   * Get paginated list of task sprints with filtering and search capabilities.
   * </p>
   * <p>
   * Retrieves sprints based on specification criteria with support for full-text search.
   * Includes project member permission checks and assembles task counts, progress, and member information.
   * </p>
   * @param spec Generic specification for filtering
   * @param pageable Pagination parameters
   * @param fullTextSearch Whether to use full-text search
   * @param match Search match patterns
   * @return Paginated results of sprints with assembled data
   */
  @Override
  public Page<TaskSprint> list(GenericSpecification<TaskSprint> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
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

        Page<TaskSprint> page = fullTextSearch
            ? taskSprintSearchRepo.find(criteria, pageable, TaskSprint.class, match)
            : taskSprintRepo.findAll(spec, pageable);
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

  /**
   * <p>
   * Find the sprint with the earliest creation date in a project.
   * </p>
   * <p>
   * Retrieves the sprint entity with the minimum (earliest) creation date for the specified project.
   * </p>
   * @param projectId Project ID
   * @return Sprint with the earliest creation date, or null if none exists
   */
  @Override
  public TaskSprint findLeastByProjectId(Long projectId) {
    return taskSprintRepo.findLeastByProjectId(projectId);
  }

  /**
   * <p>
   * Check and find a task sprint by ID.
   * </p>
   * <p>
   * Retrieves a sprint entity by its ID and throws ResourceNotFound if not found.
   * Used for validation before operations that require an existing sprint.
   * </p>
   * @param id Sprint ID
   * @return Sprint entity if found
   * @throws ResourceNotFound if the sprint is not found
   */
  @Override
  public TaskSprint checkAndFind(Long id) {
    return taskSprintRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Sprint"));
  }

  /**
   * <p>
   * Check and find multiple task sprints by a collection of IDs.
   * </p>
   * <p>
   * Retrieves all sprint entities for the given IDs and validates that all requested IDs exist.
   * Throws ResourceNotFound if any are missing.
   * </p>
   * @param ids Collection of sprint IDs
   * @return List of sprint entities if all found
   * @throws ResourceNotFound if any sprint is not found
   */
  @Override
  public List<TaskSprint> checkAndFind(Collection<Long> ids) {
    List<TaskSprint> sprints = taskSprintRepo.findAllById(ids);

    // Validate that all requested sprints were found
    if (sprints.size() != ids.size()) {
      // Find missing IDs for better error reporting
      Set<Long> foundIds = sprints.stream()
          .map(TaskSprint::getId)
          .collect(Collectors.toSet());
      Set<Long> missingIds = ids.stream()
          .filter(id -> !foundIds.contains(id))
          .collect(Collectors.toSet());

      if (!missingIds.isEmpty()) {
        throw ResourceNotFound.of(missingIds.iterator().next(), "Sprint");
      }
    }

    return sprints;
  }

  /**
   * <p>
   * Check if authorization control is enabled for a sprint.
   * </p>
   * <p>
   * Determines whether the specified sprint has authorization control enabled.
   * Returns false if the sprint does not exist.
   * </p>
   * @param id Sprint ID
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public boolean isAuthCtrl(Long id) {
    TaskSprint sprint = taskSprintRepo.findById(id).orElse(null);
    return nonNull(sprint) && sprint.getAuth();
  }

  /**
   * <p>
   * Check if a sprint name already exists in a project.
   * </p>
   * <p>
   * Validates that no sprint with the same name exists in the specified project.
   * Throws ResourceExisted if a duplicate name is found.
   * </p>
   * @param projectId Project ID
   * @param name Sprint name to check
   * @throws ResourceExisted if the name already exists
   */
  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = taskSprintRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(TASK_SPRINT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  /**
   * <p>
   * Validate sprint date range constraints.
   * </p>
   * <p>
   * Ensures that the start date is before the deadline date and that the start date
   * is at least 10 minutes in the future from the current time.
   * </p>
   * @param startDate Sprint start date
   * @param deadlineDate Sprint deadline date
   * @throws ProtocolException if date range constraints are violated
   */
  @Override
  public void checkSprintDateRange(LocalDateTime startDate, LocalDateTime deadlineDate) {
    if (startDate.isAfter(deadlineDate) || startDate.plusMinutes(10)
        .isBefore(LocalDateTime.now())) {
      throw ProtocolException.of(TASK_SPRINT_DATE_RANGE_ERROR_T,
          new Object[]{DateUtils.formatDate(DateUtils.asDate(startDate), DEFAULT_DATE_TIME_FORMAT),
              DateUtils.formatDate(DateUtils.asDate(deadlineDate), DEFAULT_DATE_TIME_FORMAT)});
    }
  }

  /**
   * <p>
   * Check if a sprint has started.
   * </p>
   * <p>
   * Validates that the sprint status indicates it is in process.
   * Throws BizException if the sprint has not started.
   * </p>
   * @param sprint Sprint to check
   * @throws BizException if the sprint has not started
   */
  @Override
  public void checkHasStarted(TaskSprint sprint) {
    if (!sprint.getStatus().isInProcess()) {
      throw BizException.of(TASK_SPRINT_NOT_STARTED_CODE, TASK_SPRINT_NOT_STARTED);
    }
  }

  /**
   * <p>
   * Check if all tasks in a sprint are completed.
   * </p>
   * <p>
   * Validates that all tasks within the specified sprint have been completed.
   * Throws ProtocolException if any tasks remain incomplete.
   * </p>
   * @param id Sprint ID
   * @throws ProtocolException if any tasks are not completed
   */
  @Override
  public void checkSprintTasksCompleted(Long id) {
    long num = taskRepo.countNotCompletedBySprintId(id);
    if (num > 0) {
      throw ProtocolException.of(TASK_SPRINT_NOT_COMPLETED);
    }
  }

  /**
   * <p>
   * Check and validate sprint quota for the current tenant.
   * </p>
   * <p>
   * Validates that creating a new sprint would not exceed the tenant's quota limits.
   * Returns the current sprint count for reference.
   * </p>
   * @return Current number of sprints for the tenant
   */
  @Override
  public int checkQuota() {
    long count = taskSprintRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterSprint, null, count + 1);
    return (int) count;
  }

  /**
   * <p>
   * Set task numbers for a list of sprints.
   * </p>
   * <p>
   * Efficiently retrieves and sets both total task counts and valid task counts for multiple sprints.
   * Uses batch database queries to avoid N+1 query problems.
   * </p>
   * @param sprints List of sprints to update
   * @param sprintIds Set of sprint IDs for batch querying
   */
  @Override
  public void setTaskNum(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      // Batch retrieve total task counts for all sprints
      Map<Long, Long> taskNumsMap = taskInfoRepo.findSprintTaskNumsGroupBySprintId(sprintIds)
          .stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));

      // Batch retrieve valid task counts for all sprints
      Map<Long, Long> validTaskNumsMap = taskInfoRepo.findValidSprintTaskNumsGroupBySprintId(sprintIds)
          .stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));

      // Set task counts for each sprint efficiently
      for (TaskSprint sprint : sprints) {
        Long sprintId = sprint.getId();
        sprint.setTaskNum(taskNumsMap.getOrDefault(sprintId, 0L));
        sprint.setValidTaskNum(validTaskNumsMap.getOrDefault(sprintId, 0L));
      }
    }
  }

  /**
   * <p>
   * Set progress information for a list of sprints.
   * </p>
   * <p>
   * Calculates and sets progress metrics including total tasks, completed tasks, and completion rate.
   * Must be executed after setTaskNum() to ensure valid task counts are available.
   * Uses batch database queries for efficiency.
   * </p>
   * @param sprints List of sprints to update
   * @param sprintIds Set of sprint IDs for batch querying
   */
  @Override
  public void setProgress(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      // Batch retrieve passed task counts for all sprints
      Map<Long, Long> sprintPassedNumsMap = taskInfoRepo.findSprintPassedTaskNumsGroupBySprintId(sprintIds)
          .stream()
          .collect(Collectors.toMap(SprintTaskNum::getSprintId, SprintTaskNum::getTaskNum));

      // Calculate and set progress for each sprint
      for (TaskSprint sprint : sprints) {
        Long sprintId = sprint.getId();
        Long validTaskNum = sprint.getValidTaskNum();
        Long passedTaskNum = sprintPassedNumsMap.getOrDefault(sprintId, 0L);

        // Calculate completion rate with proper decimal precision
        BigDecimal completedRate = validTaskNum > 0
            ? BigDecimal.valueOf(passedTaskNum)
                .divide(BigDecimal.valueOf(validTaskNum), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) // Convert to percentage
            : BigDecimal.ZERO;

        sprint.setProgress(new Progress()
            .setTotal(validTaskNum)
            .setCompleted(passedTaskNum)
            .setCompletedRate(completedRate));
      }
    }
  }

  /**
   * <p>
   * Set member information for a list of sprints.
   * </p>
   * <p>
   * Retrieves and sets member information for each sprint based on associated users.
   * Includes user details such as names and avatars for display purposes.
   * </p>
   * @param sprints List of sprints to update
   * @param sprintIds Set of sprint IDs for batch querying
   */
  @Override
  public void setMembers(List<TaskSprint> sprints, Set<Long> sprintIds) {
    if (isNotEmpty(sprints)) {
      for (TaskSprint sprint : sprints) {
        // Get associated user IDs for this sprint
        Set<Long> memberIds = taskQuery.getAssociateUsersBySprintId(sprint.getId());

        if (isNotEmpty(memberIds)) {
          // Batch retrieve user information for all members
          Map<Long, UserBase> users = userManager.getUserBaseMap(memberIds);

          // Convert UserBase to UserInfo and filter valid users
          List<UserInfo> members = memberIds.stream()
              .filter(users::containsKey)
              .map(memberId -> CoreUtils.copyProperties(users.get(memberId), new UserInfo()))
              .toList();

          sprint.setMembers(members);
        } else {
          sprint.setMembers(new ArrayList<>());
        }
      }
    }
  }

  /**
   * <p>
   * Generate a safe clone name for a sprint.
   * </p>
   * <p>
   * Creates a unique clone name by appending "-Copy" and optionally a random suffix.
   * Ensures the name does not exceed maximum length constraints while maintaining uniqueness.
   * </p>
   * @param sprint Sprint to generate clone name for
   */
  @Override
  public void setSafeCloneName(TaskSprint sprint) {
    String originalName = sprint.getName();
    String baseCloneName = originalName + "-Copy";
    String saltName = randomAlphanumeric(3);

    // Check if base clone name already exists
    String clonedName = taskSprintRepo.existsByProjectIdAndName(sprint.getProjectId(), baseCloneName)
        ? baseCloneName + "." + saltName
        : baseCloneName;

    // Ensure name length constraints are met
    if (clonedName.length() > MAX_NAME_LENGTH) {
      clonedName = clonedName.substring(0, MAX_NAME_LENGTH_X2 - 3) + saltName;
    }

    sprint.setName(clonedName);
  }

  /**
   * <p>
   * Check and set authorization object ID criteria for sprint queries.
   * </p>
   * <p>
   * Adds authorization filtering criteria when the user is not an administrator or when
   * querying only user-specific data. Ensures proper access control for sprint queries.
   * </p>
   * @param criteria Search criteria to modify
   * @return Always returns false (legacy return value)
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria) {
    // Extract admin criteria if present
    SearchCriteria adminCriteria = findFirstAndRemove(criteria, "admin");
    boolean isAdminQuery = false;

    if (Objects.nonNull(adminCriteria)) {
      String adminValue = adminCriteria.getValue().toString().replaceAll("\"", "");
      isAdminQuery = parseBoolean(adminValue);
    }

    // Add authorization filtering if not admin query or user is not admin
    if (!isAdminQuery || !taskSprintAuthQuery.isAdminUser()) {
      criteria.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }

    return false; // Legacy return value
  }

  /**
   * <p>
   * Get sprint creation summaries with filtering capabilities.
   * </p>
   * <p>
   * Retrieves sprint summaries based on various filtering criteria including project,
   * sprint, date range, and creator information. Supports comprehensive filtering
   * for reporting and analysis purposes.
   * </p>
   * @param projectId Project ID for filtering
   * @param sprintId Optional sprint ID for filtering
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param creatorOrgType Creator organization type
   * @param creatorOrgId Creator organization ID
   * @return List of sprint summaries matching the criteria
   */
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
