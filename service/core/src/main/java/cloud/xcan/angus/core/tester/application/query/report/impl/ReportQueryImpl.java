package cloud.xcan.angus.core.tester.application.query.report.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.REPORT;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.getApisResourcesStatsFilter;
import static cloud.xcan.angus.core.tester.application.query.apis.impl.ApisQueryImpl.getApisDetailSummary;
import static cloud.xcan.angus.core.tester.application.query.issue.impl.TaskQueryImpl.getSprintSummary;
import static cloud.xcan.angus.core.tester.application.query.issue.impl.TaskQueryImpl.getTaskDetailSummary;
import static cloud.xcan.angus.core.tester.application.query.scenario.impl.ScenarioQueryImpl.getScenarioDetailSummary;
import static cloud.xcan.angus.core.tester.application.query.services.impl.ServicesQueryImpl.getServicesSummary;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getCaseDetailSummary;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getPlanSummary;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getProjectSummary;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.REPORT_RECORD_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.REPORT_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.REPORT_SHARE_TOKEN_INVALID;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ReportGenerationSuccessful;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ReportGenerationSuccessfulCode;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecTestResultQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.kanban.KanbanEfficiencyQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TestEvaluationQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportAuthQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisBasicStatusSummary;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskRemarkSummary;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.report.ReportInfoRepo;
import cloud.xcan.angus.core.tester.domain.report.ReportRepo;
import cloud.xcan.angus.core.tester.domain.report.ReportResourcesCount;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordRepo;
import cloud.xcan.angus.core.tester.domain.report.record.content.ApisTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.FuncCaseContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.FuncPlanContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.TaskContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.TaskSprintContent;
import cloud.xcan.angus.core.tester.domain.report.summary.ReportCategorySummary;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.summary.ScenarioInfoSummary;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.setting.ContentFilterSetting;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@SummaryQueryRegister(name = "Report", table = "report", groupByColumns = {/*"created_date", */
    "category", "template", "status"})
@Service
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
  private TestEvaluationQuery testEvaluationQuery;

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
  private ApisTestQuery apisTestQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ScenarioTestQuery scenarioTestQuery;

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
  private ExecTestResultQuery execTestResultQuery;

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
        assertNotNull(record, REPORT_RECORD_NOT_FOUND);
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
        assertNotNull(record, REPORT_RECORD_NOT_FOUND);
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
        projectMemberQuery.checkMember(spec.getCriteria());
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
    return nonNull(reportInfo) && reportInfo.getAuth();
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
    assertResourceExisted(count <= 0, REPORT_REPEATED_T, new Object[]{name, version});
  }

  @Override
  public ActivityResource checkAndFindResource(ReportTemplate template, Long projectId,
      CombinedTargetType targetType, Long targetId) {
    //    long count = reportRepo.countByTemplateAndProjectIdAndTargetTypeAndTargetId(
    //        template, projectId, targetType, targetId);
    //    assertResourceExisted(count <= 0, REPORT_REPEATED_T, new Object[]{targetType, targetId});
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
      List<ReportInfo> authReports = new ArrayList<>();
      for (ReportInfo report : reports) {
        if (!report.getAuth() || commonQuery.isAdminUser()) {
          report.setCurrentAuths(new HashSet<>(ReportPermission.ALL));
        } else {
          authReports.add(report);
        }
      }

      if (isNotEmpty(authReports)) {
        Map<Long, List<ReportAuth>> authServiceMap = reportAuthQuery.findAuth(
                getUserId(), reports.stream().map(ReportInfo::getId).collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(ReportAuth::getReportId));
        for (ReportInfo doAuthService : authReports) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(ReportAuth::getAuths).flatMap(Collection::stream).collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public ScenarioTestingContent assembleScenarioTestingContent(ContentFilterSetting filter) {
    ScenarioTestingContent content = new ScenarioTestingContent();
    Scenario scenarioDb = scenarioQuery.detail(filter.getTargetId());
    content.setScenario(joinSupplier.execute(() -> getScenarioDetailSummary(scenarioDb)));

    List<TestType> enabledTestTypes = scenarioTestQuery.findEnabledTestTypes(filter.getTargetId());
    ExecTestResultSummary resultSummary = execTestResultQuery.assembleExecTestResultSummary(
        filter.getTargetId(), enabledTestTypes);

    content.setTestResult(resultSummary);
    return content;
  }

  @Override
  public ApisTestingContent assembleApisTestingContent(ContentFilterSetting filter) {
    ApisTestingContent content = new ApisTestingContent();
    Apis apisDb = apisQuery.detail(filter.getTargetId(), false);
    content.setApis(joinSupplier.execute(() -> getApisDetailSummary(apisDb)));

    List<TestType> enabledTestTypes = apisTestQuery.findEnabledTestTypes(filter.getTargetId());
    ExecTestResultSummary resultSummary = execTestResultQuery.assembleExecTestResultSummary(
        filter.getTargetId(), enabledTestTypes);

    content.setTestResult(resultSummary);
    return content;
  }

  @Override
  public ServicesTestingContent assembleServicesTestingContent(
      ContentFilterSetting filter, Long projectId) {
    ServicesTestingContent content = new ServicesTestingContent();
    Services servicesDb = servicesQuery.detail(filter.getTargetId(), false);
    content.setServices(joinSupplier.execute(() -> getServicesSummary(servicesDb)));

    ExecApisResultInfo testResult = execTestResultQuery.serviceApisResult(filter.getTargetId(),
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd());

    Set<Long> memberIds = projectMemberQuery.getMemberIds(filter.getCreatorObjectType(),
        filter.getCreatorObjectId(), projectId);
    Set<SearchCriteria> apisFilters = getApisResourcesStatsFilter(projectId, filter.getTargetId(),
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ApisBasicStatusSummary> apis = apisBasicInfoRepo.findProjectionByFilters(
        ApisBasicInfo.class, ApisBasicStatusSummary.class, apisFilters);

    // stc.setApis(getApisInfoSummary(apis)); // Must before at assembleServiceApisStatus()
    assembleServiceApisStatus(apis, testResult);
    content.setTestResult(testResult);
    return content;
  }

  @Override
  public FuncCaseContent assembleFuncCaseContent(ContentFilterSetting filter) {
    FuncCaseContent content = new FuncCaseContent();
    FuncCase caseDb = funcCaseQuery.detail(filter.getTargetId());
    content.setCases(joinSupplier.execute(() -> getCaseDetailSummary(caseDb)));

    List<ActivitySummary> activities = activityQuery.findSummaryByTarget(
        CombinedTargetType.FUNC_CASE, filter.getTargetId());
    content.setActivities(activities);

    List<CommentTreeSummary> comments = commentQuery.treeSummary(
        CombinedTargetType.FUNC_CASE, filter.getTargetId());
    content.setComments(comments);
    return content;
  }

  @Override
  public FuncPlanContent assembleFuncPlanContent(ContentFilterSetting filter, Long projectId) {
    FuncPlanContent content = new FuncPlanContent();
    FuncPlan planDb = funcPlanQuery.detail(filter.getPlanOrSprintId());
    content.setPlan(joinSupplier.execute(() -> getPlanSummary(planDb)));

    content.setProgress(planDb.getProgress());
    content.setMembers(planDb.getMembers());

    EfficiencyCaseOverview caseOverview = kanbanEfficiencyQuery.caseEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        filter.getPlanOrSprintId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), true, false, true, false, true
    );
    content.setCases(caseOverview);
    return content;
  }

  @Override
  public TaskContent assembleTaskContent(ContentFilterSetting filter) {
    TaskContent content = new TaskContent();
    Task taskDb = taskQuery.detail(filter.getTargetId());
    content.setTask(joinSupplier.execute(() -> getTaskDetailSummary(taskDb)));

    List<TaskRemarkSummary> remarks = joinSupplier.execute(
        () -> taskRemarkQuery.findSummaryByTaskId(filter.getTargetId()));
    content.setRemarks(remarks);

    List<ActivitySummary> activities = activityQuery.findSummaryByTarget(
        CombinedTargetType.TASK, filter.getTargetId());
    content.setActivities(activities);

    List<CommentTreeSummary> comments = commentQuery.treeSummary(
        CombinedTargetType.TASK, filter.getTargetId());
    content.setComments(comments);

    return content;
  }

  @Override
  public TaskSprintContent assembleTaskSprintContent(ContentFilterSetting filter, Long projectId) {
    TaskSprintContent content = new TaskSprintContent();
    TaskSprint sprintDb = taskSprintQuery.detail(filter.getPlanOrSprintId());
    content.setSprint(joinSupplier.execute(() -> getSprintSummary(sprintDb)));

    content.setProgress(sprintDb.getProgress());
    content.setMembers(sprintDb.getMembers());

    EfficiencyTaskOverview taskOverview = kanbanEfficiencyQuery.taskEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        filter.getPlanOrSprintId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), false, true, false, true
    );
    content.setTasks(taskOverview);
    return content;
  }

  @Override
  public ProjectProgressContent assembleProjectProgressContent(
      ContentFilterSetting filter, Long projectId) {
    ProjectProgressContent content = new ProjectProgressContent();
    Project projectDb = projectQuery.detail(projectId);
    content.setProject(getProjectSummary(projectDb));

    EfficiencyTaskOverview taskOverview = kanbanEfficiencyQuery.taskEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        /*filter.getPlanOrSprintId()*/ null, filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), false, true, false, true
    );
    content.setTasks(taskOverview);

    EfficiencyCaseOverview caseOverview = kanbanEfficiencyQuery.caseEfficiencyOverview(
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), projectId,
        /*filter.getPlanOrSprintId()*/ null, filter.getCreatedDateStart(),
        filter.getCreatedDateEnd(), true, false, true, false, true
    );
    content.setCases(caseOverview);

    ExecApisResultInfo apisResult = execTestResultQuery.projectApisResult(projectId,
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd());
    Set<Long> memberIds = projectMemberQuery.getMemberIds(filter.getCreatorObjectType(),
        filter.getCreatorObjectId(), projectId);
    Set<SearchCriteria> apisFilters = getApisResourcesStatsFilter(projectId, null,
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ApisBasicStatusSummary> apis = apisBasicInfoRepo.findProjectionByFilters(
        ApisBasicInfo.class, ApisBasicStatusSummary.class, apisFilters);
    assembleServiceApisStatus(apis, apisResult);
    content.setApis(apisResult);

    ExecScenarioResultInfo scenarioResult = execTestResultQuery.projectScenarioResult(projectId,
        filter.getCreatorObjectType(), filter.getCreatorObjectId(), filter.getCreatedDateStart(),
        filter.getCreatedDateEnd());
    Set<SearchCriteria> scenarioFilters = getCommonDeletedResourcesStatsFilter(projectId,
        filter.getCreatedDateStart(), filter.getCreatedDateEnd(), memberIds);
    List<ScenarioInfoSummary> scenarios = scenarioRepo.findProjectionByFilters(
        Scenario.class, ScenarioInfoSummary.class, scenarioFilters);
    assembleScenarioStatus(scenarios, scenarioResult);
    content.setScenarios(scenarioResult);

    Progress progress = new Progress()
        .setTotal(taskOverview.getTotalOverview().getValidTaskNum()
            + caseOverview.getTotalOverview().getValidCaseNum()
            + apisResult.getTestResultCount().getEnabledTestNum()
            + scenarioResult.getTestResultCount().getEnabledTestNum())
        .setCompleted(taskOverview.getTotalOverview().getCompletedNum()
            + caseOverview.getTotalOverview().getPassedTestNum()
            + apisResult.getTestResultCount().getTestPassedNum()
            + scenarioResult.getTestResultCount().getTestPassedNum());
    content.setProgress(progress);
    return content;
  }

  @Override
  public TestEvaluationResult assembleTestEvaluationContent(ContentFilterSetting filter,
      Long projectId) {
    TestEvaluation evaluation = new TestEvaluation();
    evaluation.setProjectId(projectId);
    evaluation.setScope(EvaluationScope.valueOf(filter.getTargetType().name()));
    evaluation.setResourceId(filter.getTargetId());
    evaluation.setStartDate(filter.getCreatedDateStart());
    evaluation.setDeadlineDate(filter.getCreatedDateEnd());
    ProtocolAssert.assertNotEmpty(filter.getEvaluationPurposes(),
        "evaluation purposes is missing");
    evaluation.setPurposes(filter.getEvaluationPurposes());
    return testEvaluationQuery.getEvaluationResult(evaluation, true);
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
