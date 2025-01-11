package cloud.xcan.sdf.core.angustester.application.query.report.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.REPORT;
import static cloud.xcan.sdf.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.sdf.core.angustester.application.converter.ApisConverter.getApisResourcesStatsFilter;
import static cloud.xcan.sdf.core.angustester.application.query.apis.impl.ApisQueryImpl.getApisDetailSummary;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getCaseDetailSummary;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getPlanSummary;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getProjectSummary;
import static cloud.xcan.sdf.core.angustester.application.query.scenario.impl.ScenarioQueryImpl.getScenarioDetailSummary;
import static cloud.xcan.sdf.core.angustester.application.query.services.impl.ServicesQueryImpl.getServicesSummary;
import static cloud.xcan.sdf.core.angustester.application.query.task.impl.TaskQueryImpl.getSprintSummary;
import static cloud.xcan.sdf.core.angustester.application.query.task.impl.TaskQueryImpl.getTaskDetailSummary;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.REPORT_RECORD_NOT_FOUND;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.REPORT_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.REPORT_RESOURCE_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.REPORT_SHARE_TOKEN_INVALID;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.ReportGenerationSuccessful;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.ReportGenerationSuccessfulCode;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.ExecResultDoorRemote;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultDetailVo;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.SettingTenantQuotaManager;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.activity.ActivityQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.comment.CommentQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.kanban.KanbanEfficiencyQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskRemarkQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.activity.summary.ActivitySummary;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.summary.ApisBasicStatusSummary;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportRepo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportResourcesCount;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuth;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordRepo;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ApisTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncCaseContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncPlanContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskSprintContent;
import cloud.xcan.sdf.core.angustester.domain.report.summary.ReportCategorySummary;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.summary.ScenarioInfoSummary;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentFilterSetting;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.event.EventSender;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.http.HttpMethod;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Slf4j
@SummaryQueryRegister(name = "Report", table = "report", groupByColumns = {/*"created_date", */
    "category", "template", "status"})
@Biz
public class ReportQueryImpl implements ReportQuery {

  @Resource
  private ReportRepo reportRepo;

  @Resource
  private ReportInfoRepo reportInfoRepo;

  @Resource
  private ReportRecordRepo reportRecordRepo;

  @Resource
  private ReportAuthQuery reportAuthQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private TaskRemarkQuery taskRemarkQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisBasicInfoRepo apisBasicInfoRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private KanbanEfficiencyQuery kanbanEfficiencyQuery;

  @Resource
  private CommentQuery commentQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ActivityQuery activityQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private UserManager userManager;

  @Resource
  private JoinSupplier joinSupplier;

  @Resource
  private ExecResultDoorRemote execResultDoorRemote;

  @Override
  public Report detail(Long id) {
    return new BizTemplate<Report>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the share report permission
        reportAuthQuery.checkViewReportAuth(getUserId(), id);
        // Check and find
        reportDb = checkAndFind(id);
      }

      @Override
      protected Report process() {
        return reportDb;
      }
    }.execute();
  }

  @Override
  public String shareToken(Long id) {
    return new BizTemplate<String>() {
      ReportInfo reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = checkAndFindInfo(id);
        // Check the share report permission
        reportAuthQuery.checkShareReportAuth(getUserId(), id);
      }

      @Override
      protected String process() {
        return reportDb.getShareToken();
      }
    }.execute();
  }

  @Override
  public Object content(Long id) {
    return new BizTemplate<>() {
      ReportInfo reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = checkAndFindInfo(id);
        // Check the share report permission
        reportAuthQuery.checkViewReportAuth(getUserId(), id);
      }

      @Override
      protected Object process() {
        ReportRecord record = reportRecordRepo.findLastByReportId(id);
        ProtocolAssert.assertNotNull(record, REPORT_RECORD_NOT_FOUND);
        return record.getContent();
      }
    }.execute();
  }

  @Override
  public Object content(Long id, String token, Long recordId) {
    return new BizTemplate<>() {
      ReportInfo reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = checkAndFindInfo(id);
        // Check the share report permission
        assertTrue(Objects.equals(token, reportDb.getShareToken()), REPORT_SHARE_TOKEN_INVALID);
      }

      @Override
      protected Object process() {
        ReportRecord record = isNull(recordId) ? reportRecordRepo.findLastByReportId(id)
            : reportRecordRepo.findById(recordId).orElse(null);
        ProtocolAssert.assertNotNull(record, REPORT_RECORD_NOT_FOUND);
        return record.getContent();
      }
    }.execute();
  }

  @Override
  public Page<ReportInfo> find(GenericSpecification<ReportInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<ReportInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<ReportInfo> process() {
        Page<ReportInfo> page = reportInfoRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }

        // Set the current user report permissions
        setReportInfoCurrentAuths(page.getContent());
        return page;
      }
    }.execute();
  }

  @Override
  public ReportResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ReportResourcesCount>() {
      final ReportResourcesCount result = new ReportResourcesCount();

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ReportResourcesCount process() {
        Set<Long> createdBys = null;
        // Find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // NOTE: countXXX() is project scope stats !!!

        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);

        // Number of statistical report
        countReport(result, commonFilters);
        reportByCategory(result, commonFilters);
        return result;
      }
    }.execute();
  }

  @Override
  public boolean isAuthCtrl(Long id) {
    ReportInfo reportInfo = reportInfoRepo.findById(id).orElse(null);
    return nonNull(reportInfo) && reportInfo.getAuthFlag();
  }

  @Override
  public void checkFilters(ReportTemplate template, ContentFilterSetting filter) {
    if (isNull(template) || isNull(filter)) {
      return;
    }
    assertTrue(!template.isWidePlan()
        || nonNull(filter.getPlanOrSprintId()), "planOrSprintId is missing");
    assertTrue(!template.isWidePlan()
            || Objects.equals(filter.getTargetId(), filter.getPlanOrSprintId()),
        "targetId and planOrSprintId are inconsistent");
  }

  @Override
  public void checkExists(Long projectId, String name, String version) {
    long count = reportRepo.countByProjectIdAndNameAndVersion(projectId, name, version);
    if (count > 0) {
      throw ResourceExisted.of(REPORT_REPEATED_T, new Object[]{name, version});
    }
  }

  @Override
  public ActivityResource checkAndFindResource(Long projectId,
      CombinedTargetType targetType, Long targetId) {
    long count = reportRepo.countByProjectIdAndTargetTypeAndTargetId(
        projectId, targetType, targetId);
    if (count > 0) {
      throw ResourceExisted.of(REPORT_RESOURCE_REPEATED_T, new Object[]{targetType, targetId});
    }
    ActivityResource resource = commonQuery.checkAndFindActivityResource(targetType, targetId);
    assertTrue(projectId.equals(resource.getProjectId()),
        String.format("Report project ID [%s] and resource project ID [%s] are inconsistent",
            projectId, resource.getProjectId()));
    return resource;
  }

  @Override
  public void checkQuota() {
    long count = reportRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterReport, null, count + 1);
  }

  @Override
  public Report checkAndFind(Long id) {
    return reportRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Report"));
  }

  @Override
  public ReportInfo checkAndFindInfo(Long id) {
    return reportInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Report"));
  }

  @Override
  public List<Report> checkAndFind(Collection<Long> ids) {
    List<Report> reports = reportRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(reports), ids.iterator().next(), "Report");
    if (ids.size() != reports.size()) {
      for (Report report : reports) {
        assertResourceNotFound(ids.contains(report.getId()), report.getId(), "Report");
      }
    }
    return reports;
  }

  @Override
  public void setReportInfoCurrentAuths(List<ReportInfo> reports) {
    if (isNotEmpty(reports)) {
      List<ReportInfo> doAuthReports = new ArrayList<>();
      for (ReportInfo report : reports) {
        if (!report.getAuthFlag() || commonQuery.isAdminUser()) {
          report.setCurrentAuths(new HashSet<>(ReportPermission.ALL));
        } else {
          doAuthReports.add(report);
        }
      }

      if (isNotEmpty(doAuthReports)) {
        Map<Long, List<ReportAuth>> authServiceMap = reportAuthQuery.findAuth(
                getUserId(), reports.stream().map(ReportInfo::getId).collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(ReportAuth::getReportId));
        for (ReportInfo doAuthService : doAuthReports) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(ReportAuth::getAuths).flatMap(Collection::stream)
                .collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public @NotNull ScenarioTestingContent assembleScenarioTestingContent(
      ContentFilterSetting filter) {
    ScenarioTestingContent stc = new ScenarioTestingContent();
    Scenario scenarioDb = scenarioQuery.detail(filter.getTargetId());
    stc.setScenario(joinSupplier.execute(() -> getScenarioDetailSummary(scenarioDb)));
    ExecTestResultVo sceTestResult = execResultDoorRemote.scenarioResult(
        filter.getTargetId()).orElseContentThrow();
    stc.setTestResult(sceTestResult);
    return stc;
  }

  @Override
  public @NotNull ApisTestingContent assembleApisTestingContent(ContentFilterSetting filter) {
    ApisTestingContent atc = new ApisTestingContent();
    Apis apisDb = apisQuery.detail(filter.getTargetId(), false);
    atc.setApis(joinSupplier.execute(() -> getApisDetailSummary(apisDb)));
    ExecTestResultVo apisTestResult = execResultDoorRemote.apisResult(
        filter.getTargetId()).orElseContentThrow();
    atc.setTestResult(apisTestResult);
    return atc;
  }

  @Override
  public @NotNull ServicesTestingContent assembleServicesTestingContent(
      ContentFilterSetting filter, Long projectId, OrgAndDateFilterDto orgAndDateFilterDto) {
    ServicesTestingContent stc = new ServicesTestingContent();
    Services servicesDb = servicesQuery.detail(filter.getTargetId(), false);
    stc.setServices(joinSupplier.execute(() -> getServicesSummary(servicesDb)));
    ExecApisResultInfo testResult = execResultDoorRemote.serviceApisResult(
        filter.getTargetId(), orgAndDateFilterDto).orElseContentThrow();
    Set<Long> memberIds = projectMemberQuery.getMemberIds(filter.getCreatorObjectType(),
        filter.getCreatorObjectId(), projectId);
    Set<SearchCriteria> apisFilters = getApisResourcesStatsFilter(projectId, filter.getTargetId(),
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ApisBasicStatusSummary> apis = apisBasicInfoRepo.findProjectionByFilters(
        ApisBasicInfo.class, ApisBasicStatusSummary.class, apisFilters);
    //stc.setApis(getApisInfoSummary(apis)); // Must before at assembleServiceApisStatus()
    assembleServiceApisStatus(apis, testResult);
    stc.setTestResult(testResult);
    return stc;
  }

  @Override
  public @NotNull FuncCaseContent assembleFuncCaseContent(ContentFilterSetting filter) {
    FuncCaseContent fcc = new FuncCaseContent();
    FuncCase caseDb = funcCaseQuery.detail(filter.getTargetId());
    fcc.setCases(joinSupplier.execute(() -> getCaseDetailSummary(caseDb)));
    List<ActivitySummary> activities = activityQuery.findSummaryByTarget(
        CombinedTargetType.FUNC_CASE, filter.getTargetId());
    fcc.setActivities(activities);
    List<CommentTreeSummary> comments = commentQuery.treeSummary(
        CombinedTargetType.FUNC_CASE, filter.getTargetId());
    fcc.setComments(comments);
    return fcc;
  }

  @Override
  public @NotNull FuncPlanContent assembleFuncPlanContent(ContentFilterSetting filter,
      Long projectId) {
    FuncPlanContent fpc = new FuncPlanContent();
    FuncPlan planDb = funcPlanQuery.detail(filter.getPlanOrSprintId());
    fpc.setPlan(joinSupplier.execute(() -> getPlanSummary(planDb)));
    fpc.setProgress(planDb.getProgress());
    fpc.setMembers(planDb.getMembers());
    EfficiencyCaseOverview caseOverview2 = kanbanEfficiencyQuery.caseEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        filter.getPlanOrSprintId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), true, false, true, false, true
    );
    fpc.setCases(caseOverview2);
    return fpc;
  }

  @Override
  public @NotNull TaskContent assembleTaskContent(ContentFilterSetting filter) {
    TaskContent task = new TaskContent();
    Task taskDb = taskQuery.detail(filter.getTargetId());
    task.setTask(joinSupplier.execute(() -> getTaskDetailSummary(taskDb)));
    List<TaskRemarkSummary> remarks = joinSupplier.execute(
        () -> taskRemarkQuery.findSummaryByTaskId(filter.getTargetId()));
    task.setRemarks(remarks);
    List<ActivitySummary> activities = activityQuery.findSummaryByTarget(
        CombinedTargetType.TASK, filter.getTargetId());
    task.setActivities(activities);
    List<CommentTreeSummary> comments = commentQuery.treeSummary(
        CombinedTargetType.TASK, filter.getTargetId());
    task.setComments(comments);
    if (taskDb.isTestTask()) {
      ExecTestResultDetailVo testResult = null;
      if (taskDb.isApiTest()) {
        testResult = execResultDoorRemote.apisResultByScriptType(taskDb.getTargetId(),
            taskDb.getTestType().toScriptType().getValue()).orElseContentThrow();
      } else if (taskDb.isScenarioTest()) {
        testResult = execResultDoorRemote.scenarioResultByScriptType(taskDb.getTargetId(),
            taskDb.getTestType().toScriptType().getValue()).orElseContentThrow();
      }
      task.setTestResult(testResult);
    }
    return task;
  }

  @Override
  public @NotNull TaskSprintContent assembleTaskSprintContent(ContentFilterSetting filter,
      Long projectId) {
    TaskSprintContent tsc = new TaskSprintContent();
    TaskSprint sprintDb = taskSprintQuery.detail(filter.getPlanOrSprintId());
    tsc.setSprint(joinSupplier.execute(() -> getSprintSummary(sprintDb)));
    tsc.setProgress(sprintDb.getProgress());
    tsc.setMembers(sprintDb.getMembers());
    EfficiencyTaskOverview taskOverview = kanbanEfficiencyQuery.taskEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        filter.getPlanOrSprintId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), false, true, false, true
    );
    tsc.setTasks(taskOverview);
    return tsc;
  }

  @Override
  public @NotNull ProjectProgressContent assembleProjectProgressContent(
      ContentFilterSetting filter, Long projectId, OrgAndDateFilterDto orgAndDateFilterDto) {
    ProjectProgressContent prc = new ProjectProgressContent();
    Project projectDb = projectQuery.detail(projectId);
    prc.setProject(getProjectSummary(projectDb));

    EfficiencyTaskOverview taskOverview = kanbanEfficiencyQuery.taskEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        /*filter.getPlanOrSprintId()*/ null, filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), false, true, false, true
    );
    prc.setTasks(taskOverview);

    EfficiencyCaseOverview caseOverview = kanbanEfficiencyQuery.caseEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        /*filter.getPlanOrSprintId()*/ null, filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), true, false, true, false, true
    );
    prc.setCases(caseOverview);

    ExecApisResultInfo apisResult = execResultDoorRemote.projectApisResult(
        projectId, orgAndDateFilterDto).orElseContentThrow();
    Set<Long> memberIds = projectMemberQuery.getMemberIds(filter.getCreatorObjectType(),
        filter.getCreatorObjectId(), projectId);
    Set<SearchCriteria> apisFilters = getApisResourcesStatsFilter(projectId, null,
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ApisBasicStatusSummary> apis = apisBasicInfoRepo.findProjectionByFilters(
        ApisBasicInfo.class, ApisBasicStatusSummary.class, apisFilters);
    assembleServiceApisStatus(apis, apisResult);
    prc.setApis(apisResult);

    ExecScenarioResultInfo scenarioResult = execResultDoorRemote.projectScenarioResult(
        projectId, orgAndDateFilterDto).orElseContentThrow();
    Set<SearchCriteria> scenarioFilters = getCommonDeletedResourcesStatsFilter(projectId,
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ScenarioInfoSummary> scenarios = scenarioRepo.findProjectionByFilters(
        Scenario.class, ScenarioInfoSummary.class, scenarioFilters);
    assembleScenarioStatus(scenarios, scenarioResult);
    prc.setScenarios(scenarioResult);

    Progress progress = new Progress()
        .setTotal(taskOverview.getTotalOverview().getValidTaskNum()
            + caseOverview.getTotalOverview().getValidCaseNum()
            + apisResult.getTestResultCount().getEnabledTestNum()
            + scenarioResult.getTestResultCount().getEnabledTestNum())
        .setCompleted(taskOverview.getTotalOverview().getCompletedNum()
            + caseOverview.getTotalOverview().getPassedTestNum()
            + apisResult.getTestResultCount().getTestPassedNum()
            + scenarioResult.getTestResultCount().getTestPassedNum());
    prc.setProgress(progress);
    return prc;
  }

  @Override
  public void assembleAndSendReportGenerationSuccessfulNoticeEvent(Report reportDb) {
    if (nonNull(reportDb.getCreatedBy())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(reportDb.getTenantId())
          .get(ReportGenerationSuccessfulCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(reportDb.getCreatedBy());
      String message = message(ReportGenerationSuccessful, new Object[]{reportDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(ReportGenerationSuccessfulCode,
          message, REPORT.getValue(), reportDb.getId().toString(), reportDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  private void countReport(ReportResourcesCount result, Set<SearchCriteria> filters) {
    result.setAllReport(reportInfoRepo.countAllByFilters(filters));
  }

  private void reportByCategory(ReportResourcesCount result, Set<SearchCriteria> filters) {
    Map<ReportCategory, Long> statusMap = reportInfoRepo.countByFiltersAndGroup(ReportInfo.class,
            ReportCategorySummary.class, filters, "category", "id").stream()
        .collect(Collectors.toMap(ReportCategorySummary::getCategory,
            ReportCategorySummary::getTotal));
    for (ReportCategory value : ReportCategory.values()) {
      result.getReportByCategory().put(value, statusMap.getOrDefault(value, 0L));
    }
  }

  private static void assembleServiceApisStatus(List<ApisBasicStatusSummary> apis,
      ExecApisResultInfo testResult) {
    if (isNotEmpty(apis)) {
      Map<ApiStatus, Integer> statusMap = apis.stream()
          .collect(Collectors.groupingBy(ApisBasicStatusSummary::getStatus)).entrySet()
          .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
      for (ApiStatus value : ApiStatus.values()) {
        testResult.getApisByStatus().put(value, statusMap.getOrDefault(value, 0));
      }

      Map<HttpMethod, Integer> methodMap = apis.stream()
          .collect(Collectors.groupingBy(ApisBasicStatusSummary::getMethod)).entrySet()
          .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
      for (HttpMethod value : HttpMethod.values()) {
        testResult.getApisByMethod().put(value, methodMap.getOrDefault(value, 0));
      }
    }
  }

  private static void assembleScenarioStatus(List<ScenarioInfoSummary> scenarios,
      ExecScenarioResultInfo testResult) {
    if (isNotEmpty(scenarios)) {
      Map<ScriptType, Integer> scriptTypeMap = scenarios.stream()
          .collect(Collectors.groupingBy(ScenarioInfoSummary::getScriptType)).entrySet()
          .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
      for (ScriptType value : ScriptType.values()) {
        testResult.getSceByScriptType().put(value, scriptTypeMap.getOrDefault(value, 0));
      }

      Map<String, Integer> pluginMap = scenarios.stream()
          .collect(Collectors.groupingBy(ScenarioInfoSummary::getPlugin)).entrySet()
          .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
      testResult.getSceByPluginName().putAll(pluginMap);
    }
  }

}
