package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.sdf.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.NO_HANDLER_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.NO_HANDLER_PERMISSION_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.sdf.api.search.SearchCriteria.equal;
import static cloud.xcan.sdf.api.search.SearchCriteria.merge;
import static cloud.xcan.sdf.api.search.SearchCriteria.notEqual;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleBackloggedTaskCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleFailureAssessmentCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleLeadTimeCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleOverdueAssessmentCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleRecentDeliveryCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleTaskProgressCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.assembleUnplannedWorkCount0;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanCtoTaskConverter.calcDailyProcessedWorkload;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleTestHits;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleBugDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleBugOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleGrowthTrendCount;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleGrowthTrendDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleProcessingEfficiencyOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleResourceCreationCount;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleResourceCreationDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskCoreKpiOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskFailureAssessmentDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleTesterBugOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleTesterSubmittedBugDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleWorkload0;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleBurnDownChartDetail;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleTaskAssigneeCount;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleTaskAssigneeProgressCount;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleTimeSeriesByFormat;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.countCreationBacklog;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.countCreationMeeting;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.countCreationSprint;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.countCreationTask;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.getTaskAssigneeResourcesFilter;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.getTaskCreatorResourcesFilter;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.toTaskDetailSummary;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskSprintConverter.toSprintSummary;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getProjectSummary;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BACKLOG_TASKS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BUGS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_BURNDOWN;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_CORE_KPI;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_FAILURES;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_GROWTH_TREND;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_HANDLING_EFFICIENCY;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_LEAD_TIME;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_OVERDUE_ASSESSMENT;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_PROGRESS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_RECENT_DELIVERY;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_RESOURCE_CREATION;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_UNPLANNED_TASKS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_ANALYSIS_TASK_WORKLOAD;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.MESSAGE_TOTAL;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_APIS_EXISTED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_NAME_EXISTED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_PARENT_CIRCULAR_REF;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_PARENT_CIRCULAR_REF_BY_SELF;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_REOPEN_REPEATED_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_REOPEN_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_SCE_EXISTED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskAssignment;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskAssignmentCode;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskModification;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskModificationCode;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskPendingConfirmation;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskPendingConfirmationCode;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_SUBMITTED_BUGS;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserFullname;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.DateUtils.asDate;
import static cloud.xcan.sdf.spec.utils.DateUtils.formatByDatePattern;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import cloud.xcan.sdf.core.angustester.application.query.task.TaskRemarkQuery;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.commonlink.associate.AssociateUserType;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.SettingTenantQuotaManager;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.TaskConverter;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisQuery;
import cloud.xcan.sdf.core.angustester.application.query.comment.CommentQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskMeetingQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskTemplate;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.comment.CommentTargetType;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesRepo;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskAssociateUser;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskListRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.cases.CaseTestHit;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCase;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BugCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BugDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.BugOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.CoreKpiCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.CoreKpiDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.CoreKpiOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.GrowthTrendCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.GrowthTrendDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.GrowthTrendOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProcessingEfficiencyCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProcessingEfficiencyDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProcessingEfficiencyOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.ResourceCreationDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.ResourceCreationOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TesterSubmittedBugCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TesterSubmittedBugDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.TesterSubmittedBugOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadOverview;
import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavourite;
import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavouriteRepo;
import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollow;
import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollowRepo;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingRepo;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskAssigneeWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskDetailSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskProjectWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSprintSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSprintWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.event.EventSender;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.sdf.core.pojo.principal.Principal;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.annotations.NonNullable;
import cloud.xcan.sdf.spec.utils.JsonUtils;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
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
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@SummaryQueryRegister(name = "Task", table = "task",
    aggregateColumns = {"id", "fail_num", "total_num", "eval_workload", "actual_workload"},
    groupByColumns = {"created_date", "task_type", "test_type", "test_type", "status",
        "priority", "exec_status", "exec_result", "exec_completed_date", "start_date",
        "deadline_date", "canceled_date", "completed_date", "processed_date"}
)
public class TaskQueryImpl implements TaskQuery {

  @Resource
  private TaskListRepo taskListRepo;

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
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ServicesRepo servicesRepo;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private CommentQuery commentQuery;

  @Resource
  private TaskRemarkQuery taskRemarkQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private AnalysisQuery analysisQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private UserManager userManager;

  @Resource
  private JoinSupplier joinSupplier;

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
        if (isUserAction()) {
          // Set follow flag
          setFollow(tasks);
          // Set favourite flag
          setFavourite(tasks);
        }
        // Set task tag id and name
        tagQuery.setTags(tasks);
        // Set reference tasks and cases
        taskFuncCaseQuery.setAssocForTask(tasks);
        // Set api target name
        setApiTargetName(tasks);
        // Set scenario target name
        setScenarioTargetName(tasks);
        // Set current user role
        setCurrentRoles(tasks);
        // Set sub task
        taskDb.setSubTasks(findSub(id));
        // Set comment num
        int commentNum = commentQuery.getCommentNum(id, CommentTargetType.TASK.getValue());
        taskDb.setCommentNum(commentNum);
        // Set remark num
        int remarkNum = taskRemarkQuery.getRemarkNum(id);
        taskDb.setRemarkNum(remarkNum);
        return taskDb;
      }
    }.execute();
  }

  @Override
  public TaskCount countStatistics(Set<SearchCriteria> criterias) {
    return new BizTemplate<TaskCount>() {
      @Override
      protected void checkParams() {
        // NOOP: Check view permission
      }

      @Override
      protected TaskCount process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));
        criterias.add(SearchCriteria.equal("sprintDeletedFlag", false));
        return taskListRepo.count(criterias);
      }
    }.execute();
  }

  @Override
  public Page<Task> find(GenericSpecification<Task> spec, PageRequest pageable) {
    return new BizTemplate<Page<Task>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<Task> process() {
        spec.getCriterias().add(SearchCriteria.equal("deletedFlag", false));
        spec.getCriterias().add(SearchCriteria.equal("sprintDeletedFlag", false));

        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriterias());
        Page<Task> page = taskListRepo.find(spec.getCriterias(), pageable, Task.class, null);

        if (page.hasContent()) {
          if (isUserAction()) {
            // Set follow flag
            setFollow(page.getContent());
            // Set favourite flag
            setFavourite(page.getContent());
          }
          // Set task tag id and name
          tagQuery.setTags(page.getContent());
          // Set current user role
          setCurrentRoles(page.getContent());
          // Set api target name
          setApiTargetName(page.getContent());
          // Set scenario target name
          setScenarioTargetName(page.getContent());
          // Set assignee name and avatar
          userManager.setUserNameAndAvatar(page.getContent(),
              "assigneeId", "assigneeName", "assigneeAvatar");
        }
        return page;
      }
    }.execute();
  }

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
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("projectId", taskDb.getProjectId()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        //Auto added by @Where
        //filters.add(SearchCriteria.equal("deletedFlag", false));
        //filters.add(SearchCriteria.equal("sprintDeletedFlag", false));

        Set<Long> associatedSubTaskIds = taskInfoRepo.findSubTaskIdsById(taskId);
        associatedSubTaskIds.add(taskId); // Exclude assoc oneself
        if (isNotEmpty(associatedSubTaskIds)) {
          filters.add(SearchCriteria.notIn("id", associatedSubTaskIds));
        }
        return taskInfoRepo.findAllByFilters(filters);
      }
    }.execute();
  }

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
        //filters.add(SearchCriteria.equal("deletedFlag", false));
        //filters.add(SearchCriteria.equal("sprintDeletedFlag", false));

        Set<Long> associatedTaskIds = taskFuncCaseQuery.findWideByTargetId(List.of(caseId))
            .stream().filter(TaskFuncCase::isTaskAssocCase).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedTaskIds.remove(caseId); // Delete unrelated IDs
        if (isNotEmpty(associatedTaskIds)) {
          filters.add(SearchCriteria.notIn("id", associatedTaskIds));
        }
        return taskInfoRepo.findAllByFilters(filters);
      }
    }.execute();
  }

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
        //filters.add(SearchCriteria.equal("deletedFlag", false));
        //filters.add(SearchCriteria.equal("sprintDeletedFlag", false));

        Set<Long> associatedTaskIds = taskFuncCaseQuery.findWideByTargetId(List.of(taskId))
            .stream().filter(TaskFuncCase::isTaskAssocTask).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedTaskIds.add(taskId); // Exclude assoc oneself
        if (isNotEmpty(associatedTaskIds)) {
          filters.add(SearchCriteria.notIn("id", associatedTaskIds));
        }
        return taskInfoRepo.findAllByFilters(filters);
      }
    }.execute();
  }

  @Override
  public List<TaskInfo> assocList(TaskType taskType, Long targetId) {
    return new BizTemplate<List<TaskInfo>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<TaskInfo> process() {
        return taskInfoRepo.findAllByTargetIdAndTaskType(targetId, taskType);
      }
    }.execute();
  }

  @Override
  public TaskLastResourceCreationCount creationResourcesStatistics(Long projectId, Long sprintId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinSprint, boolean joinMeeting) {
    return new BizTemplate<TaskLastResourceCreationCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected TaskLastResourceCreationCount process() {
        final TaskLastResourceCreationCount result = new TaskLastResourceCreationCount();

        // Find all when condition is null, else find by condition
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        // Number of statistical backlog
        Set<SearchCriteria> allTaskFilters = getTaskCreatorResourcesFilter(projectId, sprintId,
            createdDateStart, createdDateEnd, createdBys);
        List<TaskEfficiencySummary> allTasks = taskInfoRepo.findProjectionByFilters(TaskInfo.class,
            TaskEfficiencySummary.class, allTaskFilters);
        countCreationBacklog(result, allTasks);

        // Number of statistical task
        countCreationTask(result, allTasks);

        // Number of statistical sprint
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        if (joinSprint) {
          Set<SearchCriteria> sprintFilters = merge(commonFilters, equal("deletedFlag", false));
          List<TaskSprint> sprints = taskSprintRepo.findAllByFilters(sprintFilters);
          countCreationSprint(result, sprints);
        }

        // Number of statistical meeting
        if (joinMeeting) {
          List<TaskMeeting> meetings = taskMeetingRepo.findAllByFilters(commonFilters);
          countCreationMeeting(result, meetings);
        }
        return result;
      }
    }.execute();
  }

  @Override
  public List<TaskAssigneeCount> assigneeSummaryStatistics(Long projectId, Long sprintId) {
    return new BizTemplate<List<TaskAssigneeCount>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<TaskAssigneeCount> process() {
        final List<TaskAssigneeCount> assigneeCounts = new ArrayList<>();

        Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap = getAssigneeTaskMap(
            projectId, sprintId);
        if (isEmpty(assigneeTaskMap)) {
          return assigneeCounts;
        }

        Set<Long> assigneeIds = assigneeTaskMap.keySet();  // Is Full
        Map<Long, UserBase> userMaps = userManager.getUserBaseMap(assigneeIds);

        for (Long assigneeId : assigneeIds) {
          assigneeCounts.add(assembleTaskAssigneeCount(assigneeId, userMaps, assigneeTaskMap));
        }

        return assigneeCounts;
      }
    }.execute();
  }

  @Override
  public List<TaskAssigneeProgressCount> assigneeProgressStatistics(Long projectId, Long sprintId) {
    return new BizTemplate<List<TaskAssigneeProgressCount>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  @Override
  public TaskAssigneeWorkSummary assigneeWorkStatistics(Long projectId, Long sprintId,
      Long userId) {
    return new BizTemplate<TaskAssigneeWorkSummary>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  @Override
  public ProgressOverview progress(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<ProgressOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ProgressOverview process() {
        ProgressOverview overview = new ProgressOverview();

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
        ProgressCount total = assembleTaskProgressCount0(tasks);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_TASK_PROGRESS).split(","));
          ProgressDetail totalDetail = new ProgressDetail();
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
            ProgressCount assignee = assembleTaskProgressCount0(
                taskMap.getOrDefault(assigneeId, emptyList()));
            overview.getAssigneesOverview().put(assigneeId, assignee);
            if (joinDataDetail) {
              ProgressDetail assigneeDetail = new ProgressDetail();
              assigneeDetail.setName(
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public BurnDownChartOverview burndownChart(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BurnDownChartOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname(), assignee);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public WorkloadOverview workload(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<WorkloadOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public OverdueAssessmentOverview overdueAssessment(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<OverdueAssessmentOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public BugOverview bug(Long projectId, Long sprintId,
      AuthObjectType assigneeOrgType, Long assigneeOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BugOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname(), assignee,
                  total);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public ProcessingEfficiencyOverview processingEfficiency(Long projectId, Long sprintId,
      AuthObjectType assigneeOrgType, Long assigneeOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<ProcessingEfficiencyOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public CoreKpiOverview coreKpi(Long projectId, Long sprintId, AuthObjectType assigneeOrgType,
      Long assigneeOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<CoreKpiOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public FailureAssessmentOverview failureAssessment(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<FailureAssessmentOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname(), assignee);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public BackloggedOverview backloggedWork(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<BackloggedOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public RecentDeliveryOverview recentDelivery(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<RecentDeliveryOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                    assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
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

  @Override
  public LeadTimeOverview leadTime(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<LeadTimeOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public UnplannedWorkOverview unplannedWork(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<UnplannedWorkOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname());
              CoreUtils.copyProperties(assignee, assigneeDetail);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public TesterSubmittedBugOverview submittedBug(@NonNullable Long projectId,
      Long sprintId, AuthObjectType creatorOrgType, Long creatorOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<TesterSubmittedBugOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  testers.getOrDefault(testerId, new UserInfo()).getFullname(), tester);
              overview.getDataDetails().add(detail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public GrowthTrendOverview growthTrend(@NonNullable Long projectId,
      Long sprintId, AuthObjectType assigneeOrgType, Long assigneeOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinAssigneeDetail, boolean joinDataDetail) {
    return new BizTemplate<GrowthTrendOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
                  assignees.getOrDefault(assigneeId, new UserInfo()).getFullname(), assignee,
                  allTimeSeries);
              overview.getDataDetails().add(assigneeDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public ResourceCreationOverview resourceCreation(Long projectId, Long sprintId,
      AuthObjectType creatorOrgType, Long creatorOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCreatorDetail, boolean joinDataDetail) {
    return new BizTemplate<ResourceCreationOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ResourceCreationOverview process() {
        ResourceCreationOverview overview = new ResourceCreationOverview();

        List<TaskEfficiencySummary> tasks = getTaskCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<TaskSprint> sprints = taskSprintQuery.getSprintCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<TaskMeeting> meetings = taskMeetingQuery.getMeetingCreatedSummaries(
            projectId, sprintId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<Analysis> analyses = analysisQuery.getAnalysisCreatedSummaries(
            AnalysisResource.TASK, projectId, sprintId, createdDateStart, createdDateEnd,
            creatorOrgType, creatorOrgId);

        // Assignees
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.addAll(
            tasks.stream().map(TaskEfficiencySummary::getCreatedBy).collect(Collectors.toList()));
        creatorIds.addAll(
            sprints.stream().map(TaskSprint::getCreatedBy).collect(Collectors.toList()));
        creatorIds.addAll(
            meetings.stream().map(TaskMeeting::getCreatedBy).collect(Collectors.toList()));
        creatorIds.addAll(
            analyses.stream().map(Analysis::getCreatedBy).collect(Collectors.toList()));
        if (isEmpty(creatorIds)) {
          return overview;
        }

        Map<Long, UserInfo> creators = commonQuery.getUserInfoMap(creatorIds);
        overview.setCreators(creators);

        // Total overview
        ResourceCreationCount total = assembleResourceCreationCount(
            tasks, sprints, meetings, analyses);

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
          Map<Long, List<Analysis>> analysisMap = analyses.stream()
              .collect(groupingBy(Analysis::getCreatedBy));
          for (Long creatorId : creators.keySet()) {
            ResourceCreationCount creator = assembleResourceCreationCount(
                taskMap.getOrDefault(creatorId, emptyList()),
                sprintMap.getOrDefault(creatorId, emptyList()),
                meetingMap.getOrDefault(creatorId, emptyList()),
                analysisMap.getOrDefault(creatorId, emptyList()));
            overview.getCreatorOverview().put(creatorId, creator);
            if (joinDataDetail) {
              ResourceCreationDetail creatorDetail = assembleResourceCreationDetail(
                  creators.getOrDefault(creatorId, new UserInfo()).getFullname(), creator,
                  allTimeSeries);
              overview.getDataDetails().add(creatorDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public AbstractOverview assembleTaskAnalysisSnapshot(Analysis analysis) {
    AbstractOverview data = null;
    AnalysisTaskTemplate template = AnalysisTaskTemplate.valueOf(analysis.getTemplate());
    switch (template) {
      case PROGRESS:
        data = progress(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case BURNDOWN:
        data = burndownChart(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case WORKLOAD:
        data = workload(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case OVERDUE_ASSESSMENT:
        data = overdueAssessment(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case BUGS:
        data = bug(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case HANDLING_EFFICIENCY:
        data = processingEfficiency(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case CORE_KPI:
        data = coreKpi(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case FAILURES:
        data = failureAssessment(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case BACKLOG_TASKS:
        data = backloggedWork(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case RECENT_DELIVERY:
        data = recentDelivery(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case LEAD_TIME:
        data = leadTime(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case UNPLANNED_TASKS:
        data = unplannedWork(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case TASK_GROWTH_TREND:
        data = growthTrend(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
      case RESOURCE_CREATION:
        data = resourceCreation(analysis.getProjectId(), analysis.getPlanId(),
            analysis.getOrgType(), analysis.getOrgId(), analysis.getStartTime(),
            analysis.getEndTime(), analysis.getContainsUserAnalysis(),
            analysis.getContainsDataDetail());
        break;
    }
    return data;
  }

  @Override
  @SneakyThrows
  public AbstractOverview parseTaskAnalysisSnapshot(String template0, String snapshot) {
    AbstractOverview data = null;
    AnalysisTaskTemplate template = AnalysisTaskTemplate.valueOf(template0);
    switch (template) {
      case PROGRESS:
        data = JsonUtils.convert(snapshot, ProgressOverview.class);
        break;
      case BURNDOWN:
        data = JsonUtils.convert(snapshot, BurnDownChartOverview.class);
        break;
      case WORKLOAD:
        data = JsonUtils.convert(snapshot, WorkloadOverview.class);
        break;
      case OVERDUE_ASSESSMENT:
        data = JsonUtils.convert(snapshot, OverdueAssessmentOverview.class);
        break;
      case BUGS:
        data = JsonUtils.convert(snapshot, BugOverview.class);
        break;
      case HANDLING_EFFICIENCY:
        data = JsonUtils.convert(snapshot, ProcessingEfficiencyOverview.class);
        break;
      case CORE_KPI:
        data = JsonUtils.convert(snapshot, CoreKpiOverview.class);
        break;
      case FAILURES:
        data = JsonUtils.convert(snapshot, FailureAssessmentOverview.class);
        break;
      case BACKLOG_TASKS:
        data = JsonUtils.convert(snapshot, BackloggedOverview.class);
        break;
      case RECENT_DELIVERY:
        data = JsonUtils.convert(snapshot, RecentDeliveryOverview.class);
        break;
      case LEAD_TIME:
        data = JsonUtils.convert(snapshot, LeadTimeOverview.class);
        break;
      case UNPLANNED_TASKS:
        data = JsonUtils.convert(snapshot, UnplannedWorkOverview.class);
        break;
      case TASK_GROWTH_TREND:
        data = JsonUtils.convert(snapshot, GrowthTrendOverview.class);
        break;
      case RESOURCE_CREATION:
        data = JsonUtils.convert(snapshot, ResourceCreationOverview.class);
        break;
    }
    return data;
  }

  @Override
  public Task checkAndFind(Long id) {
    return taskRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Task"));
  }

  @Override
  public TaskInfo checkAndFindInfo(Long id) {
    return taskInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Task"));
  }

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

  @Override
  public List<Task> checkAndFind(Collection<Long> ids) {
    Set<Long> refTaskIds = new HashSet<>(ids);
    List<Task> tasks = taskRepo.findAllById(ids);
    Set<Long> idsDb = tasks.stream().map(Task::getId).collect(Collectors.toSet());
    refTaskIds.removeAll(idsDb);
    ProtocolAssert.assertResourceNotFound(isEmpty(refTaskIds), refTaskIds, "Task");
    return tasks;
  }

  @Override
  public Map<String, List<TaskInfo>> checkAndFindByProjectAndName(Long projectId,
      Set<String> names) {
    if (ObjectUtils.isEmpty(names)) {
      return emptyMap();
    }
    List<TaskInfo> taskDb = taskInfoRepo.findByProjectIdAndNameIn(projectId, names);
    if (ObjectUtils.isEmpty(taskDb)) {
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

  @Override
  public Map<String, List<TaskInfo>> checkAndFindByPlanAndName(Long sprintId, Set<String> names) {
    if (ObjectUtils.isEmpty(names)) {
      return emptyMap();
    }
    List<TaskInfo> taskDb = taskInfoRepo.findBySprintIdAndNameIn(sprintId, names);
    if (ObjectUtils.isEmpty(taskDb)) {
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

  @Override
  public void checkUpdateParentNotCircular(Long projectId, List<Task> tasks) {
    for (Task task : tasks) {
      if (nonNull(task.getId())) {
        ProtocolAssert.assertTrue(!Objects.equals(task.getId(), task.getParentTaskId()),
            TASK_PARENT_CIRCULAR_REF_BY_SELF);
        List<Long> subIds = findAllSubIds(projectId, List.of(task.getId()));
        ProtocolAssert.assertTrue(!subIds.contains(task.getParentTaskId()),
            TASK_PARENT_CIRCULAR_REF);
      }
    }
  }

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
      userManager.setUserNameAndAvatar(subs,
          "assigneeId", "assigneeName", "assigneeAvatar");
    }
    return subs;
  }

  @Override
  public List<TaskInfo> findAllSub(Long projectId, Collection<Long> taskIds) {
    if (isEmpty(taskIds)) {
      return Collections.emptyList();
    }
    List<TaskInfo> allTaskAndSub = new ArrayList<>();
    List<TaskInfo> projectTaskSubs;
    do {
      // Find sub tasks
      projectTaskSubs = taskInfoRepo.findByProjectIdAndParentTaskIdIn(projectId, taskIds);
      if (ObjectUtils.isNotEmpty(projectTaskSubs)) {
        allTaskAndSub.addAll(projectTaskSubs);
        taskIds = projectTaskSubs.stream().map(TaskInfo::getId).collect(Collectors.toList());
      }
    } while (ObjectUtils.isNotEmpty(projectTaskSubs));
    return allTaskAndSub;
  }

  @Override
  public List<Long> findAllSubIds(Long projectId, Collection<Long> taskIds) {
    return findAllSub(projectId, taskIds).stream().map(TaskInfo::getId)
        .collect(Collectors.toList());
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
      List<Long> confirmorIds = taskInfoRepo.findConfirmorIdBySprintId(sprintId);
      if (isNotEmpty(confirmorIds)) {
        assocUserIds.addAll(confirmorIds);
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
    List<Long> confirmorIds = taskInfoRepo.findConfirmorIdByByIdIn(taskIds);
    if (isNotEmpty(confirmorIds)) {
      assocUserIds.addAll(confirmorIds);
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
   * Only confirmor and admins are allowed to modify.
   */
  @Override
  public void checkConfirmorUserPermission(Task taskDb) {
    if (commonQuery.isAdminUser()) {
      return;
    }
    boolean isConfirmor = nonNull(taskDb.getConfirmorId())
        && taskDb.getConfirmorId().equals(getUserId());
    BizAssert.assertTrue(isConfirmor, NO_HANDLER_PERMISSION_CODE, NO_HANDLER_PERMISSION);
  }

  @Override
  public void checkTaskExists(List<Long> reqTaskIds, List<Task> taskDbs) {
    ProtocolAssert.assertResourceNotFound(isNotEmpty(taskDbs), reqTaskIds, "Task");
    Map<Long, Task> taskMap = taskDbs.stream().collect(Collectors.toMap(Task::getId, o -> o));
    reqTaskIds.forEach(reqId -> {
      Task taskDb = taskMap.get(reqId);
      ProtocolAssert.assertResourceNotFound(nonNull(taskDb), reqId, "Task");
    });
  }

  @Override
  public void checkTaskOpenStatus(List<Task> taskDbs) {
    taskDbs.forEach(task -> {
      BizAssert.assertTrue(TaskStatus.isFinished(task.getStatus()),
          TASK_REOPEN_REPEATED_CODE, TASK_REOPEN_REPEATED_T, new Object[]{task.getName()});
    });
  }

  @Override
  public void checkAddNameExists(TaskSprint sprintDb, String name) {
    if (nonNull(sprintDb)) {
      assertResourceExisted(taskInfoRepo.countBySprintIdAndName(sprintDb.getId(), name) < 1,
          TASK_NAME_EXISTED_T, new Object[]{name});
      if (isNotEmpty(sprintDb.getTaskPrefix())) {
        assertResourceExisted(taskInfoRepo.countBySprintIdAndName(sprintDb.getId(),
                sprintDb.getTaskPrefix() + name) < 1,
            TASK_NAME_EXISTED_T, new Object[]{sprintDb.getTaskPrefix() + name});
      }
    } else {
      assertResourceExisted(taskInfoRepo.countByName(name) < 1,
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

  @Override
  public void checkTargetTaskExists(Long targetId, TestType testType, TaskType taskType) {
    assertResourceExisted(taskRepo.countByTargetIdAndTestType(targetId, testType) < 1,
        TaskType.API_TEST.equals(taskType) ? TASK_APIS_EXISTED_T : TASK_SCE_EXISTED_T,
        new Object[]{targetId});
  }

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

  @Override
  public void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> tasks) {
    Set<Long> taskIds = tasks.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<TaskFavourite> favourites = taskFavouriteRepo
        .findAllByTaskIdInAndCreatedBy(taskIds, getUserId());
    Set<Long> favouritesTaskIds = favourites.stream().map(TaskFavourite::getTaskId)
        .collect(Collectors.toSet());
    tasks.forEach(api -> {
      if (favouritesTaskIds.contains(api.getId())) {
        api.setFavouriteFlag(true);
      }
    });
  }

  @Override
  public void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> tasks) {
    Set<Long> taskIds = tasks.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<TaskFollow> follows = taskFollowRepo
        .findByTaskIdInAndCreatedBy(taskIds, getUserId());
    Set<Long> followTaskIds = follows.stream().map(TaskFollow::getTaskId)
        .collect(Collectors.toSet());
    tasks.forEach(task -> {
      if (followTaskIds.contains(task.getId())) {
        task.setFollowFlag(true);
      }
    });
  }

  @Override
  public void setCurrentRoles(List<Task> tasks) {
    if (isNotEmpty(tasks)) {
      Principal principal = PrincipalContext.get();
      boolean isSysAdmin = PrincipalContext.isTenantSysAdmin();
      boolean isAppAdmin = PrincipalContext.hasPolicy(TesterConstant.ANGUSTESTER_ADMIN);

      tasks.forEach(task -> {
        List<AssociateUserType> currentRoles = new ArrayList<>();
        if (nonNull(task.getCreatedBy())
            && task.getCreatedBy().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.CREATOR);
        }
        if (nonNull(task.getAssigneeId()) && task.getAssigneeId().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.ASSIGNEE);
        }
        if (nonNull(task.getConfirmorId()) && task.getConfirmorId().equals(principal.getUserId())) {
          currentRoles.add(AssociateUserType.CONFIRMOR);
        }
        if (isSysAdmin) {
          currentRoles.add(AssociateUserType.SYS_ADMIN);
        }
        if (isAppAdmin) {
          currentRoles.add(AssociateUserType.APP_ADMIN);
        }
        if (isNotEmpty(currentRoles)) {
          task.setCurrentAssociateType(currentRoles);
        }
      });
    }
  }

  @Override
  public void setApiTargetName(List<Task> tasks) {
    if (isNotEmpty(tasks)) {
      Set<Long> targetIds = tasks.stream()
          .filter(x -> (nonNull(x.getTargetId()) && x.getTaskType().isApiTest()))
          .map(Task::getTargetId).collect(Collectors.toSet());
      if (isEmpty(targetIds)) {
        return;
      }
      Set<Long> targetParentIds = tasks.stream()
          .filter(x -> (nonNull(x.getTargetParentId()) && x.getTaskType().isApiTest()))
          .map(Task::getTargetParentId).collect(Collectors.toSet());
      if (isEmpty(targetParentIds)) {
        return;
      }
      Map<Long, ApisBaseInfo> apisDbMap = apisBaseInfoRepo.findAll0ByIdIn(targetIds).stream()
          .collect(Collectors.toMap(ApisBaseInfo::getId, x -> x));
      Map<Long, Services> projectsDbMap = servicesRepo.findAll0ByIdIn(targetParentIds).stream()
          .collect(Collectors.toMap(Services::getId, x -> x));
      for (Task task : tasks) {
        if (task.getTaskType().isApiTest()) {
          if (nonNull(task.getTargetId()) && nonNull(apisDbMap.get(task.getTargetId()))) {
            task.setTargetName(apisDbMap.get(task.getTargetId()).getName());
          }
          if (nonNull(task.getTargetParentId())
              && nonNull(projectsDbMap.get(task.getTargetParentId()))) {
            task.setTargetParentName(projectsDbMap.get(task.getTargetParentId()).getName());
          }
        }
      }
    }
  }

  @Override
  public void setScenarioTargetName(List<Task> tasks) {
    if (isNotEmpty(tasks)) {
      Set<Long> targetIds = tasks.stream()
          .filter(x -> (nonNull(x.getTargetId()) && x.getTaskType().isScenarioTest()))
          .map(Task::getTargetId).collect(Collectors.toSet());
      if (isEmpty(targetIds)) {
        return;
      }

      Map<Long, Scenario> scenariosDbMap = scenarioRepo.findAll0ByIdIn(targetIds)
          .stream().collect(Collectors.toMap(Scenario::getId, x -> x));
      for (Task task : tasks) {
        if (task.getTaskType().isScenarioTest()) {
          if (nonNull(task.getTargetId()) && nonNull(scenariosDbMap.get(task.getTargetId()))) {
            task.setTargetName(scenariosDbMap.get(task.getTargetId()).getName());
          }
        }
      }
    }
  }

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
      String message = message(TaskModification, new Object[]{getUserFullname(),
              taskDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
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
      String message = message(TaskModification, new Object[]{getUserFullname(),
              taskDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskModificationCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

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
      String message = message(TaskAssignment, new Object[]{getUserFullname(), taskDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskAssignmentCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public void assembleAndSendPendingConfirmationNoticeEvent(Task taskDb) {
    if (nonNull(taskDb.getConfirmorId())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
          nullSafe(taskDb.getTenantId(), getOptTenantId())).get(TaskPendingConfirmationCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(taskDb.getConfirmorId());
      String message = message(TaskPendingConfirmation,
          new Object[]{getUserFullname(), taskDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(TaskPendingConfirmationCode, message,
          TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  private List<TaskEfficiencySummary> getTaskEfficiencySummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType assigneeOrgType,
      Long assigneeOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> assigneeIds = isNull(assigneeOrgType) ? null
        : userManager.getUserIdByOrgType0(assigneeOrgType, assigneeOrgId);
    Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, sprintId,
        createdDateStart, createdDateEnd, assigneeIds);
    // allFilters.add(equal("backlogFlag", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class,
        TaskEfficiencySummary.class, allFilters);
  }

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
    // allFilters.add(equal("backlogFlag", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class, TaskEfficiencySummary.class,
        allFilters);
  }

  @NotNull
  private Map<Long, List<TaskEfficiencySummary>> getAssigneeTaskMap(Long projectId, Long sprintId) {
    Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, sprintId,
        null, null, null);
    // allFilters.add(equal("backlogFlag", false)); // Should be included
    return taskInfoRepo.findProjectionByFilters(TaskInfo.class, TaskEfficiencySummary.class,
        allFilters).stream().collect(groupingBy(TaskEfficiencySummary::getAssigneeId));
  }

  @NameJoin
  public static TaskSprintSummary getSprintSummary(TaskSprint sprintDb) {
    return toSprintSummary(sprintDb);
  }

  @NameJoin
  public static List<TaskSummary> getTaskSummary(List<TaskInfo> tasks) {
    return isEmpty(tasks) ? null
        : tasks.stream().map(TaskConverter::toTaskSummary).collect(Collectors.toList());
  }

  @NameJoin
  public static TaskDetailSummary getTaskDetailSummary(Task task) {
    return toTaskDetailSummary(task);
  }

}
