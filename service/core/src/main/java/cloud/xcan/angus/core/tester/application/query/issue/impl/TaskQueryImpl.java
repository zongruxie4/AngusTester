package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.NO_HANDLER_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.NO_HANDLER_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleBackloggedTaskCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleFailureAssessmentCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleLeadTimeCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleOverdueAssessmentCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleRecentDeliveryCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleTaskProgressCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleUnplannedWorkCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.calcDailyProcessedWorkload;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleTestHits;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleBugDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleBugOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleGrowthTrendCount;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleGrowthTrendDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleProcessingEfficiencyOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleResourceCreationCount;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleResourceCreationDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskCoreKpiOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskFailureAssessmentDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleTesterBugOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleTesterSubmittedBugDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleWorkload0;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleBurnDownChartDetail;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleTaskAssigneeCount;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleTaskAssigneeProgressCount;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleTimeSeriesByFormat;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.countCreationBacklog;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.countCreationMeeting;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.countCreationSprint;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.countCreationTask;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.findAllSubTaskInfos;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.findAllSubTasks;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.getTaskAssigneeResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.getTaskCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.toTaskDetailSummary;
import static cloud.xcan.angus.core.tester.application.converter.TaskSprintConverter.toSprintSummary;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getProjectSummary;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BACKLOG_TASKS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BUGS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BURNDOWN;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_CORE_KPI;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_FAILURES;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_GROWTH_TREND;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_HANDLING_EFFICIENCY;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_LEAD_TIME;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_OVERDUE_ASSESSMENT;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_PROGRESS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_RECENT_DELIVERY;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_RESOURCE_CREATION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_UNPLANNED_TASKS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_WORKLOAD;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_TOTAL;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_PARENT_CIRCULAR_REF;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_PARENT_CIRCULAR_REF_BY_SELF;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_REOPEN_REPEATED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_REOPEN_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SUB_IS_NOT_COMPLETED_T;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskAssignment;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskAssignmentCode;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskModification;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskModificationCode;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskPendingConfirmation;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskPendingConfirmationCode;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_SUBMITTED_BUGS;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.merge;
import static cloud.xcan.angus.remote.search.SearchCriteria.notEqual;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserFullName;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.DateUtils.asDate;
import static cloud.xcan.angus.spec.utils.DateUtils.formatByDatePattern;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.associate.AssociateUserType;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.converter.TaskConverter;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.TaskAssociateUser;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskListRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskSearchRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BugCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BugDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.BugOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.CoreKpiCount;
import cloud.xcan.angus.core.tester.domain.issue.count.CoreKpiDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.FailureAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.FailureAssessmentDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.FailureAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.GrowthTrendCount;
import cloud.xcan.angus.core.tester.domain.issue.count.GrowthTrendDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ProcessingEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.issue.count.ProcessingEfficiencyDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.ProcessingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.issue.count.ResourceCreationDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskAssigneeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskAssigneeProgressCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TesterSubmittedBugCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TesterSubmittedBugDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.TesterSubmittedBugOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadCount;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavourite;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollow;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowRepo;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskAssigneeWorkSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskDetailSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSprintSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSprintWorkSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSummary;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * <p>
 * Implementation of TaskQuery for comprehensive task management and query operations.
 * </p>
 * <p>
 * Provides methods for task CRUD operations, statistics analysis, progress tracking, and various task-related queries.
 * Includes support for task associations, permissions, notifications, and comprehensive reporting capabilities.
 * Supports full-text search, pagination, and complex filtering with project member permission checks.
 * </p>
 */
@Biz
@SummaryQueryRegister(name = "Task", table = "task",
    aggregateColumns = {"id", "fail_num", "total_num", "eval_workload", "actual_workload"},
    groupByColumns = {"created_date", "task_type", "status", "priority", "start_date",
        "deadline_date", "canceled_date", "completed_date", "processed_date"}
)
public class TaskQueryImpl implements TaskQuery {

  @Resource
  private TaskListRepo taskListRepo;
  @Resource
  private TaskSearchRepo taskSearchRepo;
  @Resource
  private TagQuery tagQuery;
  @Resource
  private TaskMeetingRepo taskMeetingRepo;
  @Resource
  private TaskMeetingQuery taskMeetingQuery;
  @Resource
  private TaskRepo taskRepo;
  @Resource
  private TaskInfoRepo taskInfoRepo;
  @Resource
  private TaskSprintRepo taskSprintRepo;
  @Resource
  private TaskSprintQuery taskSprintQuery;
  @Resource
  private TaskFavouriteRepo taskFavouriteRepo;
  @Resource
  private TaskFollowRepo taskFollowRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private CommentQuery commentQuery;
  @Resource
  private TaskRemarkQuery taskRemarkQuery;
  @Resource
  private ActivityQuery activityQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;
  @Resource
  private UserManager userManager;
  @Resource
  private JoinSupplier joinSupplier;

  /**
   * <p>
   * Get detailed information of a task by ID with comprehensive associated data.
   * </p>
   * <p>
   * Retrieves task details and assembles all related information including tags, associations,
   * progress, comments, remarks, and activities. Sets user-specific flags for follow and favorite status.
   * </p>
   * @param id Task ID
   * @return Complete task details with all associated information
   */
  @Override
  public Task detail(Long id) {
    return new BizTemplate<Task>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = checkAndFind(id);
        // NOOP: Check view permission
      }

      @Override
      protected Task process() {
        List<Task> tasks = List.of(taskDb);

        // Set user-specific flags only for user actions to avoid unnecessary processing
        if (isUserAction()) {
          // Set follow flag for current user
          setFollow(tasks);
          // Set favourite flag for current user
          setFavourite(tasks);
        }

        // Assemble comprehensive task data for display
        // Set task tag information (id and name)
        tagQuery.setTags(tasks);
        // Set associated tasks and functional cases
        taskFuncCaseQuery.setAssocForTask(tasks);
        // Set current user's role for each task (assignee, creator, admin, etc.)
        setCurrentRoles(tasks);

        // Set hierarchical task structure
        // Retrieve and set direct subtasks
        taskDb.setSubTasks(findSub(id));

        // Set related counts for task overview
        // Get comment count for this task
        int commentNum = commentQuery.getCommentNum(id, CommentTargetType.TASK.getValue());
        taskDb.setCommentNum(commentNum);
        // Get remark count for this task
        int remarkNum = taskRemarkQuery.getRemarkNum(id);
        taskDb.setRemarkNum(remarkNum);
        // Get activity count for this task
        int activityNum = activityQuery.getActivityNumByMainTarget(id);
        taskDb.setActivityNum(activityNum);

        // Calculate and set progress information
        // Set progress for main task
        setTaskProgress(tasks);
        // Set progress for all subtasks
        setTaskInfoProgress(taskDb.getSubTasks());

        return taskDb;
      }
    }.execute();
  }

  /**
   * <p>
   * Count task statistics based on search criteria.
   * </p>
   * <p>
   * Provides aggregated task counts for reporting and analysis purposes.
   * Automatically filters out deleted tasks and sprints.
   * </p>
   * @param criteria Search criteria for filtering tasks
   * @return Task count statistics
   */
  @Override
  public TaskCount countStatistics(Set<SearchCriteria> criteria) {
    return new BizTemplate<TaskCount>() {
      @Override
      protected void checkParams() {
        // NOOP: Check view permission
      }

      @Override
      protected TaskCount process() {
        criteria.add(SearchCriteria.equal("deleted", false));
        criteria.add(SearchCriteria.equal("sprintDeleted", false));
        return taskListRepo.count(criteria);
      }
    }.execute();
  }

  /**
   * <p>
   * List tasks with pagination and search capabilities.
   * </p>
   * <p>
   * Supports both full-text search and specification-based filtering with project member permission checks.
   * Automatically sets user-specific flags and assembles related data for each task.
   * </p>
   * @param export Whether this is an export operation
   * @param spec Generic specification for filtering
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Fields to match in full-text search
   * @return Page of tasks with associated data
   */
  @Override
  public Page<Task> list(boolean export, GenericSpecification<Task> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Task>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<Task> process() {
        // Add standard filters to exclude deleted tasks and sprints
        spec.getCriteria().add(SearchCriteria.equal("deleted", false));
        spec.getCriteria().add(SearchCriteria.equal("sprintDeleted", false));

        // Apply authorization criteria based on current user context
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

        // Execute search using appropriate repository based on search type
        Page<Task> page = fullTextSearch
            ? taskSearchRepo.find(spec.getCriteria(), pageable, Task.class, match)
            : taskListRepo.find(spec.getCriteria(), pageable, Task.class, null);

        // Enrich task data with additional information if results exist
        if (page.hasContent()) {
          // Set user-specific flags only for user actions
          if (isUserAction()) {
            // Set follow status for current user
            setFollow(page.getContent());
            // Set favourite status for current user
            setFavourite(page.getContent());
          }

          // Assemble comprehensive task data for display
          // Set task tag information
          tagQuery.setTags(page.getContent());
          // Set current user's role for each task
          setCurrentRoles(page.getContent());
          // Calculate and set progress information
          setTaskProgress(page.getContent());
          // Set assignee display information (name and avatar)
          userManager.setUserNameAndAvatar(page.getContent(),
              "assigneeId", "assigneeName", "assigneeAvatar");

          // Include additional data for export operations
          if (export) {
            // Set associated tasks and functional cases for comprehensive export
            taskFuncCaseQuery.setAssocForTask(page.getContent());
          }
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Find subtasks that are not associated with a specific task.
   * </p>
   * <p>
   * Retrieves available subtasks that can be associated with the given task.
   * Filters by module if specified and excludes already associated subtasks.
   * </p>
   * @param taskId Parent task ID
   * @param moduleId Optional module ID for filtering
   * @return List of available subtasks
   */
  @Override
  public List<TaskInfo> notAssociatedSubtask(Long taskId, Long moduleId) {
    return new BizTemplate<List<TaskInfo>>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        taskDb = checkAndFindInfo(taskId);
      }

      @Override
      protected List<TaskInfo> process() {
        // Build search criteria for available subtasks
        Set<SearchCriteria> filters = new HashSet<>();
        // Filter by project to ensure data isolation
        filters.add(SearchCriteria.equal("projectId", taskDb.getProjectId()));
        // Apply module filter if specified
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        // Note: deleted and sprintDeleted filters are automatically added by @Where annotation
        // filters.add(SearchCriteria.equal("deleted", false));
        // filters.add(SearchCriteria.equal("sprintDeleted", false));

        // Get IDs of already associated subtasks to exclude them
        Set<Long> associatedSubTaskIds = taskInfoRepo.findSubTaskIdsById(taskId);
        // Exclude the task itself to prevent self-association
        associatedSubTaskIds.add(taskId);
        if (isNotEmpty(associatedSubTaskIds)) {
          // Filter out already associated tasks
          filters.add(SearchCriteria.notIn("id", associatedSubTaskIds));
        }

        // Return available subtasks sorted by creation date (newest first)
        return taskInfoRepo.findAllByFilters(filters, Sort.by(Direction.DESC, "createdDate"));
      }
    }.execute();
  }

  /**
   * <p>
   * Find tasks that are not associated with a specific functional case.
   * </p>
   * <p>
   * Retrieves available tasks that can be associated with the given case.
   * Supports filtering by module and task type, excluding already associated tasks.
   * </p>
   * @param caseId Functional case ID
   * @param moduleId Optional module ID for filtering
   * @param taskType Optional task type for filtering
   * @return List of available tasks
   */
  @Override
  public List<TaskInfo> notAssociatedTaskInCase(Long caseId, @Nullable Long moduleId,
      @Nullable TaskType taskType) {
    return new BizTemplate<List<TaskInfo>>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        caseDb = funcCaseQuery.checkAndFindInfo(caseId);
      }

      @Override
      protected List<TaskInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("projectId", caseDb.getProjectId()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        if (nonNull(taskType)) {
          filters.add(SearchCriteria.equal("taskType", taskType));
        }
        //Auto added by @Where
        //filters.add(SearchCriteria.equal("deleted", false));
        //filters.add(SearchCriteria.equal("sprintDeleted", false));

        Set<Long> associatedTaskIds = taskFuncCaseQuery.findWideByTargetId(List.of(caseId))
            .stream().filter(TaskFuncCase::isTaskAssocCase).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedTaskIds.remove(caseId); // Delete unrelated IDs
        if (isNotEmpty(associatedTaskIds)) {
          filters.add(SearchCriteria.notIn("id", associatedTaskIds));
        }
        return taskInfoRepo.findAllByFilters(filters, Sort.by(Direction.DESC, "createdDate"));
      }
    }.execute();
  }

    /**
   * <p>
   * Find tasks that are not associated with a specific task.
   * </p>
   * <p>
   * Retrieves available tasks that can be associated with the given task.
   * Supports filtering by module and task type, excluding already associated tasks.
   * </p>
   * @param taskId task ID
   * @param moduleId Optional module ID for filtering
   * @param taskType Optional task type for filtering
   * @return List of available tasks
   */
  @Override
  public List<TaskInfo> notAssociatedTaskInTask(Long taskId, @Nullable Long moduleId,
      @Nullable TaskType taskType) {
    return new BizTemplate<List<TaskInfo>>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        taskDb = checkAndFindInfo(taskId);
      }

      @Override
      protected List<TaskInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("projectId", taskDb.getProjectId()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        if (nonNull(taskType)) {
          filters.add(SearchCriteria.equal("taskType", taskType));
        }
        //Auto added by @Where
        //filters.add(SearchCriteria.equal("deleted", false));
        //filters.add(SearchCriteria.equal("sprintDeleted", false));

        Set<Long> associatedTaskIds = taskFuncCaseQuery.findWideByTargetId(List.of(taskId))
            .stream().filter(TaskFuncCase::isTaskAssocTask).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedTaskIds.add(taskId); // Exclude assoc oneself
        if (isNotEmpty(associatedTaskIds)) {
          filters.add(SearchCriteria.notIn("id", associatedTaskIds));
        }
        return taskInfoRepo.findAllByFilters(filters, Sort.by(Direction.DESC, "createdDate"));
      }
    }.execute();
  }

  /**
   * <p>
   * Get resource creation statistics for tasks.
   * </p>
   * <p>
   * Provides comprehensive statistics about task creation including counts for tasks, sprints, meetings, and backlogs.
   * Supports filtering by project, sprint, creator, and date range.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param creatorObjectType Creator object type
   * @param creatorObjectId Creator object ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinSprint Whether to include sprint information
   * @param joinMeeting Whether to include meeting information
   * @return Resource creation statistics
   */
  @Override
  public TaskLastResourceCreationCount creationResourcesStatistics(Long projectId, Long sprintId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinSprint, boolean joinMeeting) {
    return new BizTemplate<TaskLastResourceCreationCount>() {

      @Override
      protected TaskLastResourceCreationCount process() {
        final TaskLastResourceCreationCount result = new TaskLastResourceCreationCount();

        // Resolve creator user IDs based on organization type and ID
        // If creatorObjectType is null, include all creators; otherwise filter by specific organization
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        // Calculate backlog statistics
        // Build filters for task creation analysis
        Set<SearchCriteria> allTaskFilters = getTaskCreatorResourcesFilter(projectId, sprintId,
            createdDateStart, createdDateEnd, createdBys);
        // Retrieve task efficiency summaries for analysis
        List<TaskEfficiencySummary> allTasks = taskInfoRepo.findProjectionByFilters(TaskInfo.class,
            TaskEfficiencySummary.class, allTaskFilters);
        // Count and categorize backlog items
        countCreationBacklog(result, allTasks);

        // Calculate task creation statistics
        countCreationTask(result, allTasks);

        // Calculate sprint creation statistics (optional)
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        if (joinSprint) {
          // Add deleted filter for sprints to exclude soft-deleted items
          Set<SearchCriteria> sprintFilters = merge(commonFilters, equal("deleted", false));
          List<TaskSprint> sprints = taskSprintRepo.findAllByFilters(sprintFilters);
          countCreationSprint(result, sprints);
        }

        // Calculate meeting creation statistics (optional)
        if (joinMeeting) {
          List<TaskMeeting> meetings = taskMeetingRepo.findAllByFilters(commonFilters);
          countCreationMeeting(result, meetings);
        }
        return result;
      }
    }.execute();
  }

  /**
   * <p>
   * Get assignee summary statistics for tasks.
   * </p>
   * <p>
   * Provides aggregated statistics about task distribution among assignees.
   * Includes task counts, workload distribution, and performance metrics per assignee.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @return List of assignee summary statistics
   */
  @Override
  public List<TaskAssigneeCount> assigneeSummaryStatistics(Long projectId, Long sprintId) {
    return new BizTemplate<List<TaskAssigneeCount>>() {

      @Override
      protected List<TaskAssigneeCount> process() {
        final List<TaskAssigneeCount> assigneeCounts = new ArrayList<>();

        // Get task distribution by assignee for the specified project and sprint
        Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap = getAssigneeTaskMap(
            projectId, sprintId);
        if (isEmpty(assigneeTaskMap)) {
          return assigneeCounts;
        }

        // Get all assignee IDs and retrieve their user information
        Set<Long> assigneeIds = assigneeTaskMap.keySet();  // Contains all assignee IDs
        Map<Long, UserBase> userMaps = userManager.getUserBaseMap(assigneeIds);

        // Build assignee count statistics for each assignee
        for (Long assigneeId : assigneeIds) {
          assigneeCounts.add(assembleTaskAssigneeCount(assigneeId, userMaps, assigneeTaskMap));
        }

        return assigneeCounts;
      }
    }.execute();
  }

  /**
   * <p>
   * Get assignee progress statistics for tasks.
   * </p>
   * <p>
   * Provides detailed progress analysis for each assignee including completion rates,
   * task status distribution, and time-based progress metrics.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @return List of assignee progress statistics
   */
  @Override
  public List<TaskAssigneeProgressCount> assigneeProgressStatistics(Long projectId, Long sprintId) {
    return new BizTemplate<List<TaskAssigneeProgressCount>>() {

      @Override
      protected List<TaskAssigneeProgressCount> process() {
        final List<TaskAssigneeProgressCount> assigneeProgressCounts = new ArrayList<>();

        Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap = getAssigneeTaskMap(
            projectId, sprintId);
        if (isEmpty(assigneeTaskMap)) {
          return assigneeProgressCounts;
        }

        Set<Long> assigneeIds = assigneeTaskMap.keySet();  // Is Full
        Map<Long, UserBase> userMaps = userManager.getUserBaseMap(assigneeIds);

        for (Long assigneeId : assigneeIds) {
          assigneeProgressCounts.add(
              assembleTaskAssigneeProgressCount(assigneeId, userMaps, assigneeTaskMap));
        }

        return assigneeProgressCounts;
      }
    }.execute();
  }

  /**
   * <p>
   * Get project work statistics summary.
   * </p>
   * <p>
   * Provides comprehensive work statistics for a project including task distribution,
   * progress overview, and performance metrics at the project level.
   * </p>
   * @param projectId Project ID
   * @return Project work statistics summary
   */
  @Override
  public TaskProjectWorkSummary projectWorkStatistics(Long projectId) {
    return new BizTemplate<TaskProjectWorkSummary>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        projectDb = projectQuery.detail(projectId);
      }

      @Override
      protected TaskProjectWorkSummary process() {
        TaskProjectWorkSummary summary = new TaskProjectWorkSummary();
        summary.setSummary(getProjectSummary(projectDb));
        summary.setTaskCount(countStatistics(new HashSet<>(Set.of(equal("projectId", projectId)))));

        // When there are no use plans, the default status value is set to 0
        TaskLastResourceCreationCount resourcesCount = creationResourcesStatistics(projectId, null,
            null, null, null, null, true, false);
        summary.setSprintByStatus(resourcesCount.getSprintByStatus());
        summary.setTaskByType(resourcesCount.getTaskByType());
        summary.setTaskByStatus(resourcesCount.getTaskByStatus());
        summary.setTaskByPriority(resourcesCount.getTaskByPriority());

        List<TaskSprint> sprints = taskSprintRepo.findByProjectId(projectId);
        for (TaskSprint sprintDb : sprints) {
          summary.getSprintSummaries().add(sprintWorkStatistics(sprintDb.getId()));
        }
        return summary;
      }
    }.execute();
  }

  /**
   * <p>
   * Get sprint work statistics summary.
   * </p>
   * <p>
   * Provides comprehensive work statistics for a sprint including task distribution,
   * progress tracking, and performance metrics at the sprint level.
   * </p>
   * @param sprintId Sprint ID
   * @return Sprint work statistics summary
   */
  @Override
  public TaskSprintWorkSummary sprintWorkStatistics(Long sprintId) {
    return new BizTemplate<TaskSprintWorkSummary>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        sprintDb = taskSprintQuery.detail(sprintId);
      }

      @Override
      protected TaskSprintWorkSummary process() {
        TaskSprintWorkSummary summary = new TaskSprintWorkSummary();
        summary.setSummary(joinSupplier.execute(() -> getSprintSummary(sprintDb)));
        summary.setProgress(sprintDb.getProgress());
        summary.setMembers(sprintDb.getMembers());

        summary.setTaskCount(countStatistics(new HashSet<>(
            Set.of(equal("projectId", sprintDb.getProjectId()), equal("sprintId", sprintId)))));

        // When there are no use cases, the default status value is set to 0
        TaskLastResourceCreationCount resourcesCount = creationResourcesStatistics(null, sprintId,
            null, null, null, null, false, false);
        summary.setTaskByType(resourcesCount.getTaskByType());
        summary.setTaskByStatus(resourcesCount.getTaskByStatus());
        summary.setTaskByPriority(resourcesCount.getTaskByPriority());

        Map<Long, TaskAssigneeCount> testerCountMap = assigneeSummaryStatistics(null,
            sprintId).stream().collect(Collectors.toMap(TaskAssigneeCount::getAssigneeId, x -> x));

        if (isNotEmpty(testerCountMap)) {
          List<TaskInfo> taskInfo0 = taskInfoRepo.findAllBySprintId(sprintId);
          if (isNotEmpty(taskInfo0)) {
            Map<Long, List<TaskSummary>> testCaseMap = joinSupplier.execute(
                    () -> getTaskSummary(taskInfo0)).stream().
                collect(groupingBy(TaskSummary::getAssigneeId));
            for (Entry<Long, List<TaskSummary>> entry : testCaseMap.entrySet()) {
              TaskAssigneeWorkSummary assigneeSummary = new TaskAssigneeWorkSummary();
              TaskAssigneeCount count = testerCountMap.getOrDefault(entry.getKey(),
                  new TaskAssigneeCount());
              assigneeSummary.setAssigneeId(count.getAssigneeId());
              assigneeSummary.setAssigneeName(count.getAssigneeName());
              assigneeSummary.setAssigneeAvatar(count.getAssigneeAvatar());
              count.setAssigneeId(null).setAssigneeName(null).setAssigneeAvatar(null);
              assigneeSummary.setCount(count);

              Map<LocalDateTime, List<TaskSummary>> testCasesGroup = entry.getValue().stream()
                  .collect(groupingBy(x -> x.getCreatedDate().truncatedTo(ChronoUnit.DAYS),
                      Collectors.mapping(Function.identity(), Collectors.toList())));
              Map<String, List<TaskSummary>> sumCaseByDay = testCasesGroup.entrySet().stream()
                  .collect(Collectors.toMap(x -> formatByDatePattern(asDate(x.getKey())),
                      Entry::getValue));
              for (Entry<String, List<TaskSummary>> entry0 : sumCaseByDay.entrySet()) {
                assigneeSummary.getGroupByDay().put(entry0.getKey(), entry.getValue());
              }
              summary.getAssigneeSummaries().add(assigneeSummary);
            }
          }
        }
        return summary;
      }
    }.execute();
  }

  /**
   * <p>
   * Get assignee work statistics for a specific user.
   * </p>
   * <p>
   * Provides detailed work statistics for a specific assignee including task distribution,
   * workload analysis, and performance metrics over time.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param userId User ID to get statistics for
   * @return Assignee work statistics summary
   */
  @Override
  public TaskAssigneeWorkSummary assigneeWorkStatistics(Long projectId, Long sprintId,
      Long userId) {
    return new BizTemplate<TaskAssigneeWorkSummary>() {

      @Override
      protected TaskAssigneeWorkSummary process() {
        TaskAssigneeWorkSummary summary = new TaskAssigneeWorkSummary();

        Map<Long, TaskAssigneeCount> testerCountMap = assigneeSummaryStatistics(projectId, sprintId)
            .stream().collect(Collectors.toMap(TaskAssigneeCount::getAssigneeId, x -> x));
        TaskAssigneeCount count = testerCountMap.getOrDefault(userId, new TaskAssigneeCount());
        summary.setAssigneeId(count.getAssigneeId());
        summary.setAssigneeName(count.getAssigneeName());
        summary.setAssigneeAvatar(count.getAssigneeAvatar());
        count.setAssigneeId(null).setAssigneeName(null).setAssigneeAvatar(null);
        summary.setCount(count);

        List<TaskInfo> taskInfo0 = nonNull(sprintId)
            ? taskInfoRepo.findAllBySprintIdAndAssigneeId(sprintId, userId)
            : taskInfoRepo.findAllByProjectIdAndAssigneeId(projectId, userId);

        if (isNotEmpty(taskInfo0)) {
          List<TaskSummary> tasks = joinSupplier.execute(() -> getTaskSummary(taskInfo0));
          Map<LocalDateTime, List<TaskSummary>> testCasesGroup = tasks.stream()
              .collect(groupingBy(x -> x.getCreatedDate().truncatedTo(ChronoUnit.DAYS),
                  Collectors.mapping(Function.identity(), Collectors.toList())));
          Map<String, List<TaskSummary>> sumCaseByDay = testCasesGroup.entrySet().stream()
              .collect(Collectors.toMap(x -> formatByDatePattern(asDate(x.getKey())),
                  Entry::getValue));
          for (Entry<String, List<TaskSummary>> entry : sumCaseByDay.entrySet()) {
            summary.getGroupByDay().put(entry.getKey(), entry.getValue());
          }
        }
        return summary;
      }
    }.execute();
  }

  /**
   * <p>
   * Get task progress overview with detailed analysis.
   * </p>
   * <p>
   * Provides comprehensive progress analysis including task completion rates, time series data,
   * and assignee performance metrics. Supports filtering by project, sprint, assignee, and date range.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Progress overview with analysis
   */
  @Override
  public ProgressOverview progress(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<ProgressOverview>() {

      @Override
      protected ProgressOverview process() {
        ProgressOverview overview = new ProgressOverview();

        // Retrieve task efficiency summaries for progress analysis
        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Build assignee information map for display purposes
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Calculate total progress overview across all tasks
        ProgressCount total = assembleTaskProgressCount0(tasks);
        overview.setTotalOverview(total);

        // Include detailed data for export if requested
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_PROGRESS).split(","));
          ProgressDetail totalDetail = new ProgressDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Calculate individual assignee progress if detailed view is requested
        // Skip if assigneeOrgType is USER (single user view) as total equals assignee
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))
        ) {
          // Group tasks by assignee for individual analysis
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));

          // Calculate progress for each assignee
          for (Long assigneeId : assignees.keySet()) {
            ProgressCount assignee = assembleTaskProgressCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);

            // Include assignee details for export if requested
            if (joinDataDetail) {
              ProgressDetail assigneeDetail = new ProgressDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get burndown chart overview for task analysis.
   * </p>
   * <p>
   * Provides burndown chart data for tracking task completion over time.
   * Includes ideal vs actual progress lines, remaining work analysis, and velocity metrics.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Burndown chart overview
   */
  @Override
  public BurnDownChartOverview burndownChart(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BurnDownChartOverview>() {

      @Override
      protected BurnDownChartOverview process() {
        BurnDownChartOverview overview = new BurnDownChartOverview();

        LocalDateTime safeCreatedDateStart = null, safeCreatedDateEnd = null;
        if (nonNull(sprintId)) {
          TaskInfo earliestTask = taskInfoRepo.findEarliestBySprintId(sprintId);
          if (nonNull(earliestTask)) {
            TaskSprint sprintDb = taskSprintQuery.checkAndFind(sprintId);
            safeCreatedDateStart = nullSafe(createdDateStart, earliestTask.getCreatedDate());
            TaskInfo leastTask = taskInfoRepo.findLeastBySprintId(sprintId);
            safeCreatedDateEnd = nullSafe(createdDateEnd,
                sprintDb.getDeadlineDate().isAfter(leastTask.getCreatedDate())
                    ? sprintDb.getDeadlineDate() : leastTask.getCreatedDate());
          }
        } else if (nonNull(projectId)) {
          TaskInfo task = taskInfoRepo.findEarliestByProjectId(projectId);
          if (nonNull(task)) {
            Project projectDb = projectQuery.checkAndFind(projectId);
            safeCreatedDateStart = nullSafe(createdDateStart, projectDb.getStartDate());
            safeCreatedDateEnd = nullSafe(createdDateEnd, projectDb.getDeadlineDate());
          }
        }
        if (isNull(safeCreatedDateStart)) {
          // Set the default data for web
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.NUM, new BurnDownChartCount());
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.WORKLOAD, new BurnDownChartCount());
          return overview;
        }

        List<TaskEfficiencySummary> validTasks = getTaskValidEfficiencySummaries(projectId,
            sprintId, safeCreatedDateStart, safeCreatedDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(validTasks)) {
          // Set the default data for web
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.NUM, new BurnDownChartCount());
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.WORKLOAD, new BurnDownChartCount());
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            validTasks.stream().map(TaskEfficiencySummary::getAssigneeId)
                .collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        Map<BurnDownResourceType, BurnDownChartCount> total = new HashMap<>();
        assembleTimeSeriesByFormat(total, validTasks, safeCreatedDateStart, safeCreatedDateEnd);
        overview.setTotalBurnDownCharts(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_BURNDOWN).split(","));
          BurnDownChartDetail totalDetail = assembleBurnDownChartDetail(
              message(MESSAGE_TOTAL), total);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap = validTasks.stream()
              .collect(Collectors.groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assigneeTaskMap.keySet()) {
            Map<BurnDownResourceType, BurnDownChartCount> assignee = new HashMap<>();
            assembleTimeSeriesByFormat(assignee, assigneeTaskMap.get(assigneeId),
                safeCreatedDateStart, safeCreatedDateEnd);
            overview.getAssigneesBurnDownCharts().put(assigneeId, assignee);
            if (joinDataDetail) {
              BurnDownChartDetail assigneeDetail = assembleBurnDownChartDetail(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName(), assignee);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get workload overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive workload analysis including planned vs actual workload,
   * workload distribution among assignees, and workload trends over time.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Workload overview
   */
  @Override
  public WorkloadOverview workload(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<WorkloadOverview>() {

      @Override
      protected WorkloadOverview process() {
        WorkloadOverview overview = new WorkloadOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        WorkloadCount total = new WorkloadCount();
        assembleWorkload0(tasks, total);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_WORKLOAD).split(","));
          WorkloadDetail totalDetail = new WorkloadDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            WorkloadCount assignee = new WorkloadCount();
            assembleWorkload0(taskMap.getOrDefault(assigneeId, emptyList()), assignee);
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              WorkloadDetail assigneeDetail = new WorkloadDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get overdue assessment overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive overdue assessment analysis including overdue task identification,
   * risk assessment, and mitigation strategies based on workload and deadlines.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Overdue assessment overview
   */
  @Override
  public OverdueAssessmentOverview overdueAssessment(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<OverdueAssessmentOverview>() {

      @Override
      protected OverdueAssessmentOverview process() {
        OverdueAssessmentOverview overview = new OverdueAssessmentOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        double dailyProcessedWorkload = calcDailyProcessedWorkload(tasks, DEFAULT_DAILY_WORKLOAD);
        OverdueAssessmentCount total = assembleOverdueAssessmentCount0(tasks,
            dailyProcessedWorkload);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_OVERDUE_ASSESSMENT).split(","));
          OverdueAssessmentDetail totalDetail = new OverdueAssessmentDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            List<TaskEfficiencySummary> assigneeTasks = taskMap.getOrDefault(assigneeId,
                emptyList());
            dailyProcessedWorkload = calcDailyProcessedWorkload(assigneeTasks,
                DEFAULT_DAILY_WORKLOAD);
            OverdueAssessmentCount assignee = assembleOverdueAssessmentCount0(
                assigneeTasks, dailyProcessedWorkload);
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              OverdueAssessmentDetail assigneeDetail = new OverdueAssessmentDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get bug overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive bug analysis including bug identification, severity assessment,
   * and bug distribution patterns among assignees and time periods.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Bug overview
   */
  @Override
  public BugOverview bug(Long projectId, Long sprintId,
      AuthObjectType assigneeOrgType, Long assigneeOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BugOverview>() {

      @Override
      protected BugOverview process() {
        BugOverview overview = new BugOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        BugCount total = assembleBugOverview(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_BUGS).split(","));
          BugDetail totalDetail = assembleBugDetail(message(MESSAGE_TOTAL), total, total);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            BugCount assignee = assembleBugOverview(taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              BugDetail assigneeDetail = assembleBugDetail(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName(), assignee,
                  total);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get processing efficiency overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive processing efficiency analysis including throughput metrics,
   * processing time analysis, and efficiency trends over time.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Processing efficiency overview
   */
  @Override
  public ProcessingEfficiencyOverview processingEfficiency(Long projectId, Long sprintId,
      AuthObjectType assigneeOrgType, Long assigneeOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<ProcessingEfficiencyOverview>() {

      @Override
      protected ProcessingEfficiencyOverview process() {
        ProcessingEfficiencyOverview overview = new ProcessingEfficiencyOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        ProcessingEfficiencyCount total = assembleProcessingEfficiencyOverview(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(
              message(EXPORT_ANALYSIS_TASK_HANDLING_EFFICIENCY).split(","));
          ProcessingEfficiencyDetail totalDetail = new ProcessingEfficiencyDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            ProcessingEfficiencyCount assignee = assembleProcessingEfficiencyOverview(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              ProcessingEfficiencyDetail assigneeDetail = new ProcessingEfficiencyDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get core KPI overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive core KPI analysis including key performance indicators,
   * productivity metrics, and performance benchmarks across different dimensions.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Core KPI overview
   */
  @Override
  public CoreKpiOverview coreKpi(Long projectId, Long sprintId, AuthObjectType assigneeOrgType,
      Long assigneeOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<CoreKpiOverview>() {

      @Override
      protected CoreKpiOverview process() {
        CoreKpiOverview overview = new CoreKpiOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        CoreKpiCount total = assembleTaskCoreKpiOverview(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(
              message(EXPORT_ANALYSIS_TASK_CORE_KPI).split(","));
          CoreKpiDetail totalDetail = new CoreKpiDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            CoreKpiCount assignee = assembleTaskCoreKpiOverview(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              CoreKpiDetail assigneeDetail = new CoreKpiDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get failure assessment overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive failure assessment analysis including failure rate analysis,
   * risk assessment, and failure pattern identification across different dimensions.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Failure assessment overview
   */
  @Override
  public FailureAssessmentOverview failureAssessment(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<FailureAssessmentOverview>() {

      @Override
      protected FailureAssessmentOverview process() {
        FailureAssessmentOverview overview = new FailureAssessmentOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        FailureAssessmentCount total = assembleFailureAssessmentCount0(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(
              message(EXPORT_ANALYSIS_TASK_FAILURES).split(","));
          FailureAssessmentDetail totalDetail = assembleTaskFailureAssessmentDetail(
              message(MESSAGE_TOTAL), total);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            FailureAssessmentCount assignee = assembleFailureAssessmentCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              FailureAssessmentDetail assigneeDetail = assembleTaskFailureAssessmentDetail(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName(), assignee);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get backlogged work overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive backlog analysis including pending task identification,
   * backlog trends, and workload distribution analysis.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Backlogged work overview
   */
  @Override
  public BackloggedOverview backloggedWork(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BackloggedOverview>() {

      @Override
      protected BackloggedOverview process() {
        BackloggedOverview overview = new BackloggedOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        BackloggedCount total = assembleBackloggedTaskCount0(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(
              message(EXPORT_ANALYSIS_TASK_BACKLOG_TASKS).split(","));
          BackloggedDetail totalDetail = new BackloggedDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            BackloggedCount assignee = assembleBackloggedTaskCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              BackloggedDetail assigneeDetail = new BackloggedDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get recent delivery overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive recent delivery analysis including delivery trends,
   * completion patterns, and delivery performance metrics.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Recent delivery overview
   */
  @Override
  public RecentDeliveryOverview recentDelivery(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<RecentDeliveryOverview>() {

      @Override
      protected RecentDeliveryOverview process() {
        RecentDeliveryOverview overview = new RecentDeliveryOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        Map<String, RecentDeliveryCount> total = assembleRecentDeliveryCount0(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(
              message(EXPORT_ANALYSIS_TASK_RECENT_DELIVERY).split(","));
          for (Entry<String, RecentDeliveryCount> entry : total.entrySet()) {
            RecentDeliveryDetail totalDetail = new RecentDeliveryDetail();
            totalDetail.setName(message(MESSAGE_TOTAL));
            CoreUtils.copyProperties(total.get(entry.getKey()), totalDetail);
            List<RecentDeliveryDetail> details = new ArrayList<>();
            details.add(totalDetail);
            overview.getDataDetails().put(entry.getKey(), details);
          }
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            Map<String, RecentDeliveryCount> assignee = assembleRecentDeliveryCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              for (Entry<String, RecentDeliveryCount> entry : assignee.entrySet()) {
                RecentDeliveryDetail assigneeDetail = new RecentDeliveryDetail();
                assigneeDetail.setName(
                    assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
                CoreUtils.copyProperties(assignee.get(entry.getKey()), assigneeDetail);
                overview.getDataDetails().get(entry.getKey()).add(assigneeDetail);
              }
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get lead time overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive lead time analysis including cycle time metrics,
   * delivery time analysis, and process efficiency measurements.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Lead time overview
   */
  @Override
  public LeadTimeOverview leadTime(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<LeadTimeOverview>() {

      @Override
      protected LeadTimeOverview process() {
        LeadTimeOverview overview = new LeadTimeOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        LeadTimeCount total = assembleLeadTimeCount0(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_LEAD_TIME).split(","));
          LeadTimeDetail totalDetail = new LeadTimeDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            LeadTimeCount assignee = assembleLeadTimeCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              LeadTimeDetail assigneeDetail = new LeadTimeDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get unplanned work overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive unplanned work analysis including ad-hoc task identification,
   * unplanned work trends, and impact assessment on planned work.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Unplanned work overview
   */
  @Override
  public UnplannedWorkOverview unplannedWork(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<UnplannedWorkOverview>() {

      @Override
      protected UnplannedWorkOverview process() {
        UnplannedWorkOverview overview = new UnplannedWorkOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        double dailyProcessedWorkload = calcDailyProcessedWorkload(tasks, DEFAULT_DAILY_WORKLOAD);
        UnplannedWorkCount total = assembleUnplannedWorkCount0(tasks, dailyProcessedWorkload);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_UNPLANNED_TASKS).split(","));
          UnplannedWorkDetail totalDetail = new UnplannedWorkDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            List<TaskEfficiencySummary> assigneeTasks = taskMap.getOrDefault(assigneeId,
                emptyList());
            dailyProcessedWorkload = calcDailyProcessedWorkload(assigneeTasks,
                DEFAULT_DAILY_WORKLOAD);
            UnplannedWorkCount assignee = assembleUnplannedWorkCount0(
                assigneeTasks, dailyProcessedWorkload);
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              UnplannedWorkDetail assigneeDetail = new UnplannedWorkDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get tester submitted bug overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive bug submission analysis including bug discovery patterns,
   * tester performance metrics, and bug quality assessment.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param creatorOrgType Creator organization type
   * @param creatorOrgId Creator organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Tester submitted bug overview
   */
  @Override
  public TesterSubmittedBugOverview submittedBug(@NonNullable Long projectId,
      Long sprintId, AuthObjectType creatorOrgType, Long creatorOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<TesterSubmittedBugOverview>() {

      @Override
      protected TesterSubmittedBugOverview process() {
        TesterSubmittedBugOverview overview = new TesterSubmittedBugOverview();

        List<TaskEfficiencySummary> tasks = getTaskCreatedSummaries(projectId,
            sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getCreatedBy).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        TesterSubmittedBugCount total = assembleTesterBugOverview(tasks);
        List<FuncCaseEfficiencySummary> cases = funcCaseQuery.getCaseEfficiencySummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<CaseTestHit> testHits = null;
        if (isEmpty(cases)) {
          testHits = taskFuncCaseQuery.findCaseHitBugs(
              cases.stream().map(FuncCaseEfficiencySummary::getId).collect(Collectors.toSet()));
          // Test hits
          assembleTestHits(cases.size(), testHits, total);
        }
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_SUBMITTED_BUGS).split(","));
          TesterSubmittedBugDetail totalDetail = assembleTesterSubmittedBugDetail(
              message(MESSAGE_TOTAL), total);
          overview.getDataDetails().add(totalDetail);
        }

        // Tester overview
        if (joinAssigneeDetail && (isNull(creatorOrgType)
            || !AuthObjectType.USER.equals(creatorOrgType)) // Total is equal assignee
        ) {
          Map<Long, List<CaseTestHit>> testHitsMap = isEmpty(testHits) ? emptyMap()
              : testHits.stream().collect(groupingBy(CaseTestHit::getBugCreatedBy));
          Map<Long, List<FuncCaseEfficiencySummary>> testerCasesMap =
              cases.stream().collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTesterId));
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getCreatedBy));
          for (Long testerId : testers.keySet()) {
            TesterSubmittedBugCount tester = assembleTesterBugOverview(
                taskMap.getOrDefault(testerId, emptyList()));

            assembleTestHits(testerCasesMap.getOrDefault(testerId, emptyList()).size(),
                testHitsMap.getOrDefault(testerId, emptyList()), tester);
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              TesterSubmittedBugDetail detail = assembleTesterSubmittedBugDetail(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName(), tester);
              overview.getDataDetails().add(detail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get growth trend overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive growth trend analysis including task growth patterns,
   * productivity trends, and performance evolution over time.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinAssigneeDetail Whether to include assignee details
   * @param joinDataDetail Whether to include detailed data
   * @return Growth trend overview
   */
  @Override
  public GrowthTrendOverview growthTrend(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<GrowthTrendOverview>() {

      @Override
      protected GrowthTrendOverview process() {
        GrowthTrendOverview overview = new GrowthTrendOverview();

        List<TaskEfficiencySummary> tasks = getTaskEfficiencySummaries(projectId, sprintId,
            createdDateStart, createdDateEnd, assigneeOrgType, assigneeOrgId);
        if (isEmpty(tasks)) {
          return overview;
        }

        // Assignees
        Map<Long, UserInfo> assignees = commonQuery.getUserInfoMap(
            tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(Collectors.toSet()));
        overview.setAssignees(assignees);

        // Total overview
        GrowthTrendCount total = assembleGrowthTrendCount(tasks);
        overview.setTotalOverview(total);
        List<DataAssetsTimeSeries> allTimeSeries = new ArrayList<>();
        if (joinDataDetail) {
          allTimeSeries = total.getTimeSeries().getOrDefault("TOTAL", emptyList());
          GrowthTrendDetail totalDetail = assembleGrowthTrendDetail(
              message(MESSAGE_TOTAL), total, allTimeSeries);
          overview.getDataDetails().add(totalDetail);
          String[] times = total.getTimeSeries().get("TOTAL").stream()
              .map(DataAssetsTimeSeries::getTimeSeries).toArray(String[]::new);
          overview.setDataDetailTitles(Stream.concat(
                  Arrays.stream(message(EXPORT_ANALYSIS_TASK_GROWTH_TREND).split(",")),
                  Arrays.stream(times))
              .toArray(String[]::new));
        }

        // Assignees overview
        if (joinAssigneeDetail && (isNull(assigneeOrgType)
            || !AuthObjectType.USER.equals(assigneeOrgType))// Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
          for (Long assigneeId : assignees.keySet()) {
            GrowthTrendCount assignee = assembleGrowthTrendCount(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              GrowthTrendDetail assigneeDetail = assembleGrowthTrendDetail(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullName(), assignee,
                  allTimeSeries);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Get resource creation overview for task analysis.
   * </p>
   * <p>
   * Provides comprehensive resource creation analysis including task, sprint, and meeting
   * creation patterns, creator productivity metrics, and resource utilization trends.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param creatorOrgType Creator organization type
   * @param creatorOrgId Creator organization ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param joinCreatorDetail Whether to include creator details
   * @param joinDataDetail Whether to include detailed data
   * @return Resource creation overview
   */
  @Override
  public ResourceCreationOverview resourceCreation(Long projectId, Long sprintId,
      AuthObjectType creatorOrgType, Long creatorOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCreatorDetail, boolean joinDataDetail) {
    return new BizTemplate<ResourceCreationOverview>() {

      @Override
      protected ResourceCreationOverview process() {
        ResourceCreationOverview overview = new ResourceCreationOverview();

        List<TaskEfficiencySummary> tasks = getTaskCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<TaskSprint> sprints = taskSprintQuery.getSprintCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<TaskMeeting> meetings = taskMeetingQuery.getMeetingCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);

        // Assignees
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.addAll(tasks.stream().map(TaskEfficiencySummary::getCreatedBy).toList());
        creatorIds.addAll(sprints.stream().map(TaskSprint::getCreatedBy).toList());
        creatorIds.addAll(meetings.stream().map(TaskMeeting::getCreatedBy).toList());
        if (isEmpty(creatorIds)) {
          return overview;
        }

        Map<Long, UserInfo> creators = commonQuery.getUserInfoMap(creatorIds);
        overview.setCreators(creators);

        // Total overview
        ResourceCreationCount total = assembleResourceCreationCount(tasks, sprints, meetings);

        overview.setTotalOverview(total);
        List<DataAssetsTimeSeries> allTimeSeries = new ArrayList<>();
        if (joinDataDetail) {
          allTimeSeries = total.getTimeSeries().getOrDefault("TOTAL", emptyList());
          ResourceCreationDetail totalDetail = assembleResourceCreationDetail(
              message(MESSAGE_TOTAL), total, allTimeSeries);
          overview.getDataDetails().add(totalDetail);
          String[] times = total.getTimeSeries().get("TOTAL").stream()
              .map(DataAssetsTimeSeries::getTimeSeries).toArray(String[]::new);
          overview.setDataDetailTitles(Stream.concat(
                  Arrays.stream(message(EXPORT_ANALYSIS_TASK_RESOURCE_CREATION).split(",")),
                  Arrays.stream(times))
              .toArray(String[]::new));
        }

        // Assignees overview
        if (joinCreatorDetail && (isNull(creatorOrgType)
            || !AuthObjectType.USER.equals(creatorOrgType)) // Total is equal assignee
        ) {
          Map<Long, List<TaskEfficiencySummary>> taskMap = tasks.stream()
              .collect(groupingBy(TaskEfficiencySummary::getCreatedBy));
          Map<Long, List<TaskSprint>> sprintMap = sprints.stream()
              .collect(groupingBy(TaskSprint::getCreatedBy));
          Map<Long, List<TaskMeeting>> meetingMap = meetings.stream()
              .collect(groupingBy(TaskMeeting::getCreatedBy));
          for (Long creatorId : creators.keySet()) {
            ResourceCreationCount creator = assembleResourceCreationCount(
                taskMap.getOrDefault(creatorId, emptyList()),
                sprintMap.getOrDefault(creatorId, emptyList()),
                meetingMap.getOrDefault(creatorId, emptyList()));
            overview.getCreatorOverview().put(creatorId, creator);
            if (joinDataDetail) {
              ResourceCreationDetail creatorDetail = assembleResourceCreationDetail(
                  creators.getOrDefault(creatorId, new UserInfo()).getFullName(), creator,
                  allTimeSeries);
              overview.getDataDetails().add(creatorDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  /**
   * <p>
   * Find the task with the earliest creation date in a project.
   * </p>
   * <p>
   * Retrieves the task info entity with the minimum (earliest) creation date for the specified project.
   * </p>
   * @param projectId Project ID
   * @return TaskInfo with the earliest creation date, or null if none exists
   */
  @Override
  public TaskInfo findLeastByProjectId(Long projectId) {
    return taskInfoRepo.findLeastByProjectId(projectId);
  }

  /**
   * <p>
   * Check and find a task info by ID.
   * </p>
   * <p>
   * Retrieves a task info entity by its ID and throws ResourceNotFound if not found.
   * </p>
   * @param id TaskInfo ID
   * @return TaskInfo entity if found
   * @throws ResourceNotFound if the task info is not found
   */
  @Override
  public TaskInfo checkAndFindInfo(Long id) {
    return taskInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Task"));
  }

  /**
   * <p>
   * Check and find multiple task info entities by a collection of IDs.
   * </p>
   * <p>
   * Retrieves all task info entities for the given IDs and validates that all requested IDs exist.
   * Throws ResourceNotFound if any are missing.
   * </p>
   * @param ids Collection of TaskInfo IDs
   * @return List of TaskInfo entities if all found
   * @throws ResourceNotFound if any task info is not found
   */
  @Override
  public List<TaskInfo> checkAndFindInfo(Collection<Long> ids) {
    List<TaskInfo> tasks = taskInfoRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(tasks), ids.iterator().next(), "Task");
    if (ids.size() != tasks.size()) {
      for (TaskInfo task : tasks) {
        assertResourceNotFound(ids.contains(task.getId()), task.getId(), "Task");
      }
    }
    return tasks;
  }

  /**
   * <p>
   * Check and find multiple tasks by a collection of IDs.
   * </p>
   * <p>
   * Retrieves all task entities for the given IDs and validates that all requested IDs exist.
   * Throws ResourceNotFound if any are missing.
   * </p>
   * @param ids Collection of Task IDs
   * @return List of Task entities if all found
   * @throws ResourceNotFound if any task is not found
   */
  @Override
  public List<Task> checkAndFind(Collection<Long> ids) {
    Set<Long> refTaskIds = new HashSet<>(ids);
    List<Task> tasks = taskRepo.findAllById(ids);
    Set<Long> idsDb = tasks.stream().map(Task::getId).collect(Collectors.toSet());
    refTaskIds.removeAll(idsDb);
    assertResourceNotFound(isEmpty(refTaskIds), refTaskIds, "Task");
    return tasks;
  }

  /**
   * <p>
   * Check and find tasks by project and a set of names.
   * </p>
   * <p>
   * Retrieves all task info entities in the specified project with names in the given set.
   * Throws ResourceNotFound if any name is not found.
   * </p>
   * @param projectId Project ID
   * @param names Set of task names to find
   * @return Map from task name to list of TaskInfo entities
   * @throws ResourceNotFound if any name is not found
   */
  @Override
  public Map<String, List<TaskInfo>> checkAndFindByProjectAndName(Long projectId,
      Set<String> names) {
    if (isEmpty(names)) {
      return emptyMap();
    }
    List<TaskInfo> taskDb = taskInfoRepo.findByProjectIdAndNameIn(projectId, names);
    if (isEmpty(taskDb)) {
      throw ResourceNotFound.of(names.iterator().next(), "Task");
    }
    if (names.size() != taskDb.size()) {
      Collection<String> namesDb = taskDb.stream()
          .map(TaskInfo::getName).collect(Collectors.toSet());
      names.removeAll(namesDb);
      throw ResourceNotFound.of(names.iterator().next(), "Task");
    }
    return taskDb.stream().collect(Collectors.groupingBy(TaskInfo::getName));
  }

  /**
   * <p>
   * Check and find tasks by sprint and a set of names.
   * </p>
   * <p>
   * Retrieves all task info entities in the specified sprint with names in the given set.
   * Throws ResourceNotFound if any name is not found.
   * </p>
   * @param sprintId Sprint ID
   * @param names Set of task names to find
   * @return Map from task name to list of TaskInfo entities
   * @throws ResourceNotFound if any name is not found
   */
  @Override
  public Map<String, List<TaskInfo>> checkAndFindByPlanAndName(Long sprintId, Set<String> names) {
    if (isEmpty(names)) {
      return emptyMap();
    }
    List<TaskInfo> taskDb = taskInfoRepo.findBySprintIdAndNameIn(sprintId, names);
    if (isEmpty(taskDb)) {
      throw ResourceNotFound.of(names.iterator().next(), "Task");
    }
    if (names.size() != taskDb.size()) {
      Collection<String> namesDb = taskDb.stream()
          .map(TaskInfo::getName).collect(Collectors.toSet());
      names.removeAll(namesDb);
      throw ResourceNotFound.of(names.iterator().next(), "Task");
    }
    return taskDb.stream().collect(Collectors.groupingBy(TaskInfo::getName));
  }

  /**
   * <p>
   * Check if all subtasks of a task are completed.
   * </p>
   * <p>
   * Validates that all subtasks of the specified task have been completed.
   * Throws an exception if any subtask is not completed, preventing parent task completion.
   * </p>
   * @param projectId Project ID
   * @param id Task ID
   */
  @Override
  public void checkSubTasksIsCompleted(Long projectId, Long id) {
    TaskInfo notCompletedTask = findSub(id).stream()
        .filter(x -> !x.getStatus().isFinished()).findFirst()
        .orElse(null);
    assertTrue(isNull(notCompletedTask), TASK_SUB_IS_NOT_COMPLETED_T,
        new Object[]{nonNull(notCompletedTask) ? notCompletedTask.getName() : null});
  }

  /**
   * <p>
   * Check that updating parent tasks does not create circular references.
   * </p>
   * <p>
   * Validates that setting parent tasks does not create circular dependencies.
   * Prevents infinite loops in task hierarchies by checking for circular references.
   * </p>
   * @param projectId Project ID
   * @param tasks List of tasks to check for circular references
   */
  @Override
  public void checkUpdateParentNotCircular(Long projectId, List<Task> tasks) {
    for (Task task : tasks) {
      if (nonNull(task.getId())) {
        assertTrue(!Objects.equals(task.getId(), task.getParentTaskId()),
            TASK_PARENT_CIRCULAR_REF_BY_SELF);
        List<Long> subIds = findAllSubIds(projectId, List.of(task.getId()));
        assertTrue(!subIds.contains(task.getParentTaskId()), TASK_PARENT_CIRCULAR_REF);
      }
    }
  }

  /**
   * <p>
   * Find direct subtasks of a task.
   * </p>
   * <p>
   * Retrieves all direct subtasks of the specified task.
   * Returns only immediate children, not nested subtasks.
   * </p>
   * @param taskId Parent task ID
   * @return List of direct subtasks
   */
  @Override
  public List<TaskInfo> findSub(Long taskId) {
    List<TaskInfo> subs = taskInfoRepo.findByParentTaskId(taskId);
    if (isNotEmpty(subs)) {
      if (isUserAction()) {
        // Set follow flag
        setFollow(subs);
        // Set favourite flag
        setFavourite(subs);
      }
      // Set assignee name and avatar
      userManager.setUserNameAndAvatar(subs, "assigneeId", "assigneeName", "assigneeAvatar");
    }
    return subs;
  }

  /**
   * <p>
   * Find all subtask information for multiple tasks.
   * </p>
   * <p>
   * Efficiently retrieves all subtask information for multiple parent tasks.
   * Uses batch processing to avoid N+1 query problems and includes nested subtasks.
   * </p>
   * @param projectId Project ID
   * @param taskIds Collection of parent task IDs
   * @return List of all subtask information
   */
  @Override
  public List<TaskInfo> findAllSubInfo(Long projectId, Collection<Long> taskIds) {
    if (isEmpty(taskIds)) {
      return Collections.emptyList();
    }
    List<TaskInfo> allTaskAndSub = new ArrayList<>();
    List<TaskInfo> projectTaskSubs;
    do {
      // Find sub tasks
      projectTaskSubs = taskInfoRepo.findByProjectIdAndParentTaskIdIn(projectId, taskIds);
      if (isNotEmpty(projectTaskSubs)) {
        allTaskAndSub.addAll(projectTaskSubs);
        taskIds = projectTaskSubs.stream().map(TaskInfo::getId).toList();
      }
    } while (isNotEmpty(projectTaskSubs));
    return allTaskAndSub;
  }

  @Override
  public List<Task> findAllSub(Long projectId, Collection<Long> taskIds) {
    if (isEmpty(taskIds)) {
      return Collections.emptyList();
    }
    List<Task> allTaskAndSub = new ArrayList<>();
    List<Task> projectTaskSubs;
    do {
      // Find sub tasks
      projectTaskSubs = taskRepo.findByProjectIdAndParentTaskIdIn(projectId, taskIds);
      if (isNotEmpty(projectTaskSubs)) {
        allTaskAndSub.addAll(projectTaskSubs);
        taskIds = projectTaskSubs.stream().map(Task::getId).toList();
      }
    } while (isNotEmpty(projectTaskSubs));
    return allTaskAndSub;
  }

  @Override
  public List<Long> findAllSubIds(Long projectId, Collection<Long> taskIds) {
    return findAllSubInfo(projectId, taskIds).stream().map(TaskInfo::getId)
        .toList();
  }

  @Override
  public Long countByProjectId(Long id) {
    return taskInfoRepo.countAll0ByProjectId(id);
  }

  @Override
  public List<Long> findAllIdByProjectIdIn(List<Long> projectIds) {
    return taskInfoRepo.findAll0IdByProjectIdIn(projectIds);
  }

  @Override
  public Set<Long> getAssociateUsersBySprintId(Long sprintId) {
    Set<Long> assocUserIds = new HashSet<>();
    List<Long> creatorIds = taskInfoRepo.findCreatorIdBySprintId(sprintId);
    if (isNotEmpty(creatorIds)) {
      assocUserIds.addAll(creatorIds);
      List<Long> assigneeIds = taskInfoRepo.findAssigneeIdBySprintId(sprintId);
      if (isNotEmpty(assigneeIds)) {
        assocUserIds.addAll(assigneeIds);
      }
      List<Long> confirmerIds = taskInfoRepo.findConfirmerIdBySprintId(sprintId);
      if (isNotEmpty(confirmerIds)) {
        assocUserIds.addAll(confirmerIds);
      }
    }
    return assocUserIds;
  }

  @Override
  public Set<Long> getAssociateUsersByTaskInfo(List<? extends TaskAssociateUser> tasksDb) {
    Set<Long> assocUserIds = new HashSet<>();
    for (TaskAssociateUser task : tasksDb) {
      assocUserIds.add(task.getCreatedBy());
    }
    Set<Long> taskIds = tasksDb.stream().map(TaskAssociateUser::getTaskId)
        .collect(Collectors.toSet());
    List<Long> assigneeIds = taskInfoRepo.findAssigneeIdByIdIn(taskIds);
    if (isNotEmpty(assigneeIds)) {
      assocUserIds.addAll(assigneeIds);
    }
    List<Long> confirmerIds = taskInfoRepo.findConfirmerIdByByIdIn(taskIds);
    if (isNotEmpty(confirmerIds)) {
      assocUserIds.addAll(confirmerIds);
    }
    return assocUserIds;
  }

  @Override
  public boolean hasModifyAttachments(List<Attachment> attachments, Task taskDb) {
    if (isEmpty(attachments) && isEmpty(taskDb.getAttachments())) {
      return false;
    }
    if ((isEmpty(attachments) && isNotEmpty(taskDb.getAttachments()))
        || (isNotEmpty(attachments) && isEmpty(taskDb.getAttachments()))
        || taskDb.getAttachments().size() != attachments.size()) {
      return true;
    }
    for (Attachment attachment : attachments) {
      if (!taskDb.getAttachments().contains(attachment)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Only assignee and admins are allowed to modify.
   */
  @Override
  public void checkAssigneeUserPermission(Task taskDb) {
    if (commonQuery.isAdminUser()) {
      return;
    }
    boolean isAssignee = taskDb.getAssigneeId().equals(getUserId());
    BizAssert.assertTrue(isAssignee, NO_HANDLER_PERMISSION_CODE, NO_HANDLER_PERMISSION);
  }

  /**
   * Only confirmer and admins are allowed to modify.
   */
  @Override
  public void checkConfirmerUserPermission(Task taskDb) {
    if (commonQuery.isAdminUser()) {
      return;
    }
    boolean isConfirmer = nonNull(taskDb.getConfirmerId())
        && taskDb.getConfirmerId().equals(getUserId());
    BizAssert.assertTrue(isConfirmer, NO_HANDLER_PERMISSION_CODE, NO_HANDLER_PERMISSION);
  }

  /**
   * <p>
   * Check that all requested task IDs exist in the database.
   * </p>
   * <p>
   * Validates that all requested task IDs correspond to existing tasks.
   * Throws an exception if any task ID does not exist, ensuring data integrity.
   * </p>
   * @param reqTaskIds List of requested task IDs
   * @param taskDbs List of found tasks from database
   */
  @Override
  public void checkTaskExists(List<Long> reqTaskIds, List<Task> taskDbs) {
    assertResourceNotFound(isNotEmpty(taskDbs), reqTaskIds, "Task");
    Map<Long, Task> taskMap = taskDbs.stream().collect(Collectors.toMap(Task::getId, o -> o));
    reqTaskIds.forEach(reqId -> {
      Task taskDb = taskMap.get(reqId);
      assertResourceNotFound(nonNull(taskDb), reqId, "Task");
    });
  }

  /**
   * <p>
   * Check that all tasks are in an open status.
   * </p>
   * <p>
   * Validates that all tasks are in a modifiable state (not completed, canceled, etc.).
   * Throws an exception if any task is not open, preventing modifications to closed tasks.
   * </p>
   * @param taskDbs List of tasks to check
   */
  @Override
  public void checkTaskOpenStatus(List<Task> taskDbs) {
    taskDbs.forEach(task -> {
      BizAssert.assertTrue(TaskStatus.isFinished(task.getStatus()),
          TASK_REOPEN_REPEATED_CODE, TASK_REOPEN_REPEATED_T, new Object[]{task.getName()});
    });
  }

  /**
   * <p>
   * Check if a task name already exists when adding a new task.
   * </p>
   * <p>
   * Validates that the task name is unique within the project and sprint scope.
   * Throws an exception if the name already exists, preventing duplicate task names.
   * </p>
   * @param projectId Project ID
   * @param sprintDb Sprint information
   * @param name Task name to check
   */
  @Override
  public void checkAddNameExists(Long projectId, TaskSprint sprintDb, String name) {
    if (nonNull(sprintDb)) {
      assertResourceExisted(taskInfoRepo.countBySprintIdAndName(sprintDb.getId(), name) < 1,
          TASK_NAME_EXISTED_T, new Object[]{name});
      if (isNotEmpty(sprintDb.getTaskPrefix())) {
        assertResourceExisted(taskInfoRepo.countBySprintIdAndName(sprintDb.getId(),
                sprintDb.getTaskPrefix() + name) < 1,
            TASK_NAME_EXISTED_T, new Object[]{sprintDb.getTaskPrefix() + name});
      }
    } else {
      assertResourceExisted(taskInfoRepo.countByProjectIdAndName(projectId, name) < 1,
          TASK_NAME_EXISTED_T, new Object[]{name});
    }
  }

  @Override
  public void checkUpdateNameExists(Long projectId, Long sprintId, String name, Long id) {
    if (isEmpty(name)) {
      return;
    }
    assertResourceExisted(nonNull(sprintId)
            ? taskInfoRepo.countBySprintIdAndNameAndIdNot(sprintId, name, id) < 1
            // Fix:: Update backlog sprint is null
            : taskInfoRepo.countByProjectIdAndNameAndIdNot(projectId, name, id) < 1,
        TASK_NAME_EXISTED_T, new Object[]{name});
  }

  /**
   * <p>
   * Check if adding tasks exceeds the sprint quota.
   * </p>
   * <p>
   * Validates that adding the specified number of tasks does not exceed the sprint's task quota.
   * Throws an exception if quota would be exceeded, preventing quota violations.
   * </p>
   * @param springId Sprint ID
   * @param incr Number of tasks to add
   */
  @Override
  public void checkQuota(Long springId, int incr) {
    long count = taskRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterTask, null, incr + count);
    if (nonNull(springId)) {
      count = taskRepo.countBySprintId(springId);
      settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterSprintTask,
          Set.of(springId), incr + count);
    }
  }

  /**
   * <p>
   * Set favorite flags for a list of tasks.
   * </p>
   * <p>
   * Efficiently loads and sets favorite status for multiple tasks to avoid N+1 query problems.
   * Uses batch retrieval and mapping to set favorite flags for the current user.
   * </p>
   * @param tasks List of tasks to set favorite flags for
   */
  @Override
  public void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> tasks) {
    // Extract task IDs for batch querying to avoid N+1 problem
    Set<Long> taskIds = tasks.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());

    // Batch retrieve favourite records for current user and specified tasks
    List<TaskFavourite> favourites = taskFavouriteRepo
        .findAllByTaskIdInAndCreatedBy(taskIds, getUserId());

    // Create a set of favourited task IDs for efficient lookup
    Set<Long> favouritesTaskIds = favourites.stream().map(TaskFavourite::getTaskId)
        .collect(Collectors.toSet());

    // Set favourite flag for tasks that are favourited by current user
    tasks.forEach(task -> {
      if (favouritesTaskIds.contains(task.getId())) {
        task.setFavourite(true);
      }
    });
  }

  /**
   * <p>
   * Set follow flags for a list of tasks.
   * </p>
   * <p>
   * Efficiently loads and sets follow status for multiple tasks to avoid N+1 query problems.
   * Uses batch retrieval and mapping to set follow flags for the current user.
   * </p>
   * @param tasks List of tasks to set follow flags for
   */
  @Override
  public void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> tasks) {
    // Extract task IDs for batch querying to avoid N+1 problem
    Set<Long> taskIds = tasks.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());

    // Batch retrieve follow records for current user and specified tasks
    List<TaskFollow> follows = taskFollowRepo
        .findByTaskIdInAndCreatedBy(taskIds, getUserId());

    // Create a set of followed task IDs for efficient lookup
    Set<Long> followTaskIds = follows.stream().map(TaskFollow::getTaskId)
        .collect(Collectors.toSet());

    // Set follow flag for tasks that are followed by current user
    tasks.forEach(task -> {
      if (followTaskIds.contains(task.getId())) {
        task.setFollow(true);
      }
    });
  }

  /**
   * <p>
   * Set current user roles for a list of tasks.
   * </p>
   * <p>
   * Determines and sets the current user's role for each task based on project membership,
   * task assignment, and user permissions. Includes roles like assignee, creator, and viewer.
   * </p>
   * @param tasks List of tasks to set roles for
   */
  @Override
  public void setCurrentRoles(List<Task> tasks) {
    if (isNotEmpty(tasks)) {
      // Get current user context and admin status
      Principal principal = PrincipalContext.get();
      boolean isSysAdmin = isTenantSysAdmin();
      boolean isAppAdmin = hasPolicy(TesterConstant.ANGUSTESTER_ADMIN);

      // Determine current user's role for each task
      tasks.forEach(task -> {
        List<AssociateUserType> currentRoles = new ArrayList<>();

        // Check if current user is the task creator
        if (nonNull(task.getCreatedBy())
            && task.getCreatedBy().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.CREATOR);
        }

        // Check if current user is the task assignee
        if (nonNull(task.getAssigneeId()) && task.getAssigneeId().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.ASSIGNEE);
        }

        // Check if current user is the task confirmer
        if (nonNull(task.getConfirmerId()) && task.getConfirmerId().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.CONFIRMER);
        }

        // Add system admin role if applicable
        if (isSysAdmin) {
          currentRoles.add(AssociateUserType.SYS_ADMIN);
        }

        // Add application admin role if applicable
        if (isAppAdmin) {
          currentRoles.add(AssociateUserType.APP_ADMIN);
        }

        // Set roles only if any are found
        if (isNotEmpty(currentRoles)) {
          task.setCurrentAssociateType(currentRoles);
        }
      });
    }
  }

  /**
   * <p>
   * Set progress information for a list of tasks.
   * </p>
   * <p>
   * Calculates and sets progress percentages for multiple tasks based on their completion status.
   * Handles various task states and calculates accurate progress metrics.
   * </p>
   * @param tasks List of tasks to set progress for
   */
  @Override
  public void setTaskProgress(List<Task> tasks) {
    if (isEmpty(tasks)) {
      return;
    }

    // Retrieve all subtasks for the project to calculate hierarchical progress
    List<Task> allSubTasks = findAllSub(tasks.get(0).getProjectId(),
        tasks.stream().map(Task::getId).collect(Collectors.toSet()));

    // If no subtasks exist, calculate simple progress for each task
    if (isEmpty(allSubTasks)) {
      for (Task task : tasks) {
        task.setProgress(new Progress()
            .setCompleted(task.getStatus().isCompleted() ? 1 : 0)
            .setTotal(!task.getStatus().isCanceled() ? 1 : 0)
        );
      }
      return;
    }

    // Calculate hierarchical progress including subtasks
    for (Task task : tasks) {
      // Find all subtasks for this specific task
      List<Task> subs = findAllSubTasks(allSubTasks, task.getId());
      subs.add(task); // Include the task itself in progress calculation

      // Calculate progress based on completed vs total tasks
      task.setProgress(new Progress()
          .setCompleted(subs.stream().filter(x -> x.getStatus().isCompleted())
              .collect(Collectors.toSet()).size())
          .setTotal(subs.stream().filter(x -> !x.getStatus().isCanceled())
              .collect(Collectors.toSet()).size()));
    }
  }

  /**
   * <p>
   * Set progress information for a list of task info objects.
   * </p>
   * <p>
   * Calculates and sets progress percentages for multiple task info objects based on their completion status.
   * Handles various task states and calculates accurate progress metrics for lightweight task representations.
   * </p>
   * @param tasks List of task info objects to set progress for
   */
  @Override
  public void setTaskInfoProgress(List<TaskInfo> tasks) {
    if (isEmpty(tasks)) {
      return;
    }
    List<TaskInfo> allSubTasks = findAllSubInfo(tasks.get(0).getProjectId(),
        tasks.stream().map(TaskInfo::getId).collect(Collectors.toSet()));
    if (isEmpty(allSubTasks)) {
      for (TaskInfo task : tasks) {
        task.setProgress(new Progress()
            .setCompleted(task.getStatus().isCompleted() ? 1 : 0)
            .setTotal(!task.getStatus().isCanceled() ? 1 : 0)
        );
      }
      return;
    }

    for (TaskInfo task : tasks) {
      List<TaskInfo> subs = findAllSubTaskInfos(allSubTasks, task.getId());
      subs.add(task);
      task.setProgress(new Progress()
          .setCompleted(subs.stream().filter(x -> x.getStatus().isCompleted())
              .collect(Collectors.toSet()).size())
          .setTotal(subs.stream().filter(x -> !x.getStatus().isCanceled())
              .collect(Collectors.toSet()).size()));
    }
  }

  /**
   * <p>
   * Assemble and send task modification notice events.
   * </p>
   * <p>
   * Creates and sends notification events for task modifications to relevant users.
   * Handles batch processing of multiple tasks and activities.
   * </p>
   * @param tasksDb List of modified tasks
   * @param activities List of related activities
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(List<Task> tasksDb, List<Activity> activities) {
    Map<Long, Activity> taskActivityMap = activities.stream()
        .collect(Collectors.toMap(Activity::getTargetId, x -> x));
    for (Task taskDb : tasksDb) {
      assembleAndSendModifyNoticeEvent(taskDb, taskActivityMap.get(taskDb.getId()));
    }
  }

  @Override
  public void assembleAndSendModifyNoticeEvent(Task taskDb, Activity activity) {
    // Get notification types configured for task modification events
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
        nullSafe(taskDb.getTenantId(), getOptTenantId())).get(TaskModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }

    // Build list of users to notify
    List<Long> receiveObjectIds = new ArrayList<>();
    // Add task assignee
    receiveObjectIds.add(taskDb.getAssigneeId());
    // Add users following this task
    List<Long> followUserIds = taskFollowRepo.findUserIdsByTaskId(taskDb.getId());
    receiveObjectIds.addAll(followUserIds);
    // Remove current user to avoid self-notification
    receiveObjectIds.remove(getUserId());

    // Send notification if there are recipients
    if (isNotEmpty(receiveObjectIds)) {
      // Create notification message with task details
      String message = message(TaskModification, new Object[]{getUserFullName(),
              taskDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());

      // Assemble and send notification event
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskModificationCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public void assembleAndSendModifyNoticeEvent(TaskInfo taskDb, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
        nullSafe(taskDb.getTenantId(), getOptTenantId())).get(TaskModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(taskDb.getAssigneeId());
    List<Long> followUserIds = taskFollowRepo.findUserIdsByTaskId(taskDb.getId());
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());
    if (isNotEmpty(receiveObjectIds)) {
      String message = message(TaskModification, new Object[]{getUserFullName(),
              taskDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskModificationCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  /**
   * <p>
   * Assemble and send task assignee modification notice event.
   * </p>
   * <p>
   * Creates and sends notification events when a task assignee is changed.
   * Notifies both the previous and new assignees about the change.
   * </p>
   * @param taskDb Modified task
   */
  @Override
  public void assembleAndSendModifyAssigneeNoticeEvent(Task taskDb) {
    if (nonNull(taskDb.getAssigneeId())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
          nullSafe(taskDb.getTenantId(), getOptTenantId())).get(TaskAssignmentCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(taskDb.getAssigneeId());
      String message = message(TaskAssignment, new Object[]{getUserFullName(), taskDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskAssignmentCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public void assembleAndSendPendingConfirmationNoticeEvent(Task taskDb) {
    if (nonNull(taskDb.getConfirmerId())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
          nullSafe(taskDb.getTenantId(), getOptTenantId())).get(TaskPendingConfirmationCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(taskDb.getConfirmerId());
      String message = message(TaskPendingConfirmation,
          new Object[]{getUserFullName(), taskDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskPendingConfirmationCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  /**
   * <p>
   * Get task efficiency summaries for analysis.
   * </p>
   * <p>
   * Retrieves task efficiency data for performance analysis and reporting.
   * Supports filtering by project, sprint, assignee, and date range.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param assigneeOrgType Assignee organization type
   * @param assigneeOrgId Assignee organization ID
   * @return List of task efficiency summaries
   */
  private List<TaskEfficiencySummary> getTaskEfficiencySummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType assigneeOrgType,
      Long assigneeOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> assigneeIds = isNull(assigneeOrgType) ? null
        : userManager.getUserIdByOrgType0(assigneeOrgType, assigneeOrgId);
    Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, sprintId,
        createdDateStart, createdDateEnd, assigneeIds);
    // allFilters.add(equal("backlog", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class,
        TaskEfficiencySummary.class, allFilters);
  }

  /**
   * <p>
   * Get task creation summaries for analysis.
   * </p>
   * <p>
   * Retrieves task creation data for performance analysis and reporting.
   * Supports filtering by project, sprint, creator, and date range.
   * </p>
   * @param projectId Project ID
   * @param sprintId Optional sprint ID
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param creatorOrgType Creator organization type
   * @param creatorOrgId Creator organization ID
   * @return List of task creation summaries
   */
  private List<TaskEfficiencySummary> getTaskCreatedSummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getTaskCreatorResourcesFilter(projectId, sprintId,
        createdDateStart, createdDateEnd, creatorIds);
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class, TaskEfficiencySummary.class,
        allFilters);
  }

  private List<TaskEfficiencySummary> getTaskValidEfficiencySummaries(
      @NonNullable Long projectId, Long sprintId, LocalDateTime safeCreatedDateStart,
      LocalDateTime safeCreatedDateEnd, AuthObjectType assigneeOrgType, Long assigneeOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> assigneeIds = isNull(assigneeOrgType) ? null
        : userManager.getUserIdByOrgType0(assigneeOrgType, assigneeOrgId);
    Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, sprintId,
        safeCreatedDateStart, safeCreatedDateEnd, assigneeIds);
    allFilters.add(notEqual("status", "CANCELED"));
    // allFilters.add(equal("backlog", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class, TaskEfficiencySummary.class,
        allFilters);
  }

  @NotNull
  private Map<Long, List<TaskEfficiencySummary>> getAssigneeTaskMap(Long projectId, Long sprintId) {
    Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, sprintId,
        null, null, null);
    // allFilters.add(equal("backlog", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class, TaskEfficiencySummary.class,
        allFilters).stream().collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
  }

  /**
   * <p>
   * Convert TaskSprint to TaskSprintSummary.
   * </p>
   * <p>
   * Static utility method to convert a TaskSprint entity to its summary representation.
   * </p>
   * @param sprintDb TaskSprint entity
   * @return TaskSprintSummary representation
   */
  @NameJoin
  public static TaskSprintSummary getSprintSummary(TaskSprint sprintDb) {
    return toSprintSummary(sprintDb);
  }

  /**
   * <p>
   * Convert list of TaskInfo to TaskSummary.
   * </p>
   * <p>
   * Static utility method to convert a list of TaskInfo entities to their summary representations.
   * </p>
   * @param tasks List of TaskInfo entities
   * @return List of TaskSummary representations
   */
  @NameJoin
  public static List<TaskSummary> getTaskSummary(List<TaskInfo> tasks) {
    return isEmpty(tasks) ? null
        : tasks.stream().map(TaskConverter::toTaskSummary).toList();
  }

  /**
   * <p>
   * Convert Task to TaskDetailSummary.
   * </p>
   * <p>
   * Static utility method to convert a Task entity to its detailed summary representation.
   * </p>
   * @param task Task entity
   * @return TaskDetailSummary representation
   */
  @NameJoin
  public static TaskDetailSummary getTaskDetailSummary(Task task) {
    return toTaskDetailSummary(task);
  }

  /**
   * <p>
   * Check and find a task by ID.
   * </p>
   * <p>
   * Retrieves a task entity by its ID and throws ResourceNotFound if not found.
   * </p>
   * @param id Task ID
   * @return Task entity if found
   * @throws ResourceNotFound if the task is not found
   */
  @Override
  public Task checkAndFind(Long id) {
    return taskRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Task"));
  }

}
