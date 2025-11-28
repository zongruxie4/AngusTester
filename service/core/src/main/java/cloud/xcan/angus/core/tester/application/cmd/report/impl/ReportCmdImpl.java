package cloud.xcan.angus.core.tester.application.cmd.report.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.REPORT;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ReportConverter.toExampleDomain;
import static cloud.xcan.angus.core.tester.application.converter.ReportConverter.toGeneratedRecord;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.lengthSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportCmd;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportRecordCmd;
import cloud.xcan.angus.core.tester.application.converter.ReportConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportAuthQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportRepo;
import cloud.xcan.angus.core.tester.domain.report.ReportStatus;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.record.content.ExecutionContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.setting.ContentFilterSetting;
import cloud.xcan.angus.core.tester.domain.setting.TimeSetting;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Biz
public class ReportCmdImpl extends CommCmd<Report, Long> implements ReportCmd {

  @Resource
  private ReportRepo reportRepo;

  @Resource
  private ReportQuery reportQuery;

  @Resource
  private ProjectQuery projectQuery;


  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ReportAuthCmd reportAuthCmd;

  @Resource
  private ReportAuthQuery reportAuthQuery;

  @Resource
  private ReportRecordCmd reportRecordCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private CommonQuery commonQuery;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Report report) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ActivityResource resourceDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectQuery.checkAndFind(report.getProjectId());
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), report.getProjectId());
        // Check the filter parameter valid by template
        ContentFilterSetting filter = report.getContentSetting().getFilter();
        reportQuery.checkFilters(report.getTemplate(), filter);
        // Check the report exists
        reportQuery.checkExists(report.getProjectId(), report.getName(), report.getVersion());
        // Check the report resource exists
        resourceDb = reportQuery.checkAndFindResource(report.getTemplate(), report.getProjectId(),
            filter.getTargetType(), filter.getTargetId());
        // Check the quota limit
        reportQuery.checkQuota();
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = add0(report, resourceDb);

        // Init report creator auth
        Long currentUserId = getUserId();
        reportAuthCmd.addCreatorAuth(idKey.getId(), Set.of(currentUserId));

        activityCmd.add(toActivity(REPORT, report, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Override
  public IdKey<Long, Object> add0(Report report, ActivityResource resourceDb) {
    ContentFilterSetting filter = report.getContentSetting().getFilter();
    TimeSetting timeSetting = report.getCreateTimeSetting();
    report.setTargetName(resourceDb.getName())
        .setCreatedAt(timeSetting.getCreatedAt())
        .setNextGenerationDate(timeSetting.getNextDate())
        .setTargetType(filter.getTargetType()).setTargetId(filter.getTargetId());
    if (report.getTemplate().isWidePlan() && isNull(filter.getPlanOrSprintId())) {
      filter.setPlanOrSprintId(filter.getTargetId());
    }
    return insert(report);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Report report) {
    new BizTemplate<Void>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = reportQuery.checkAndFind(report.getId());
        // Check the filter parameter valid by template
        reportQuery.checkFilters(report.getTemplate(), report.getContentSetting().getFilter());
        // Check the report name exists
        if (isNotEmpty(report.getName()) && isNotEmpty(report.getVersion())
            && !reportDb.getName().equals(report.getName())
            && !reportDb.getVersion().equals(report.getVersion())) {
          reportQuery.checkExists(reportDb.getProjectId(), report.getName(), report.getVersion());
        }
        // Check the target cannot be modified
        if (nonNull(report.getContentSetting())) {
          ContentFilterSetting filter = report.getContentSetting().getFilter();
          ProtocolAssert.assertTrue(reportDb.getTargetType().equals(filter.getTargetType())
              && reportDb.getTargetId().equals(filter.getTargetId()), "Target cannot be modified");
          report.setTargetType(filter.getTargetType()).setTargetId(filter.getTargetId());
        }
        // Check the report permission
        reportAuthQuery.checkModifyReportAuth(getUserId(), report.getId());
      }

      @Override
      protected Void process() {
        if (nonNull(report.getCreateTimeSetting())) {
          TimeSetting timeSetting = report.getCreateTimeSetting();
          report.setCreatedAt(timeSetting.getCreatedAt())
              .setNextGenerationDate(timeSetting.getNextDate());
        }

        reportRepo.save(copyPropertiesIgnoreNull(report, reportDb));

        activityCmd.add(toActivity(REPORT, reportDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Report report) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        if (nonNull(report.getId())) {
          // Check the report exists
          reportDb = reportQuery.checkAndFind(report.getId());
          // Check the filter parameter valid by template
          reportQuery.checkFilters(report.getTemplate(), report.getContentSetting().getFilter());
          // Check the report name exists
          if (isNotEmpty(report.getName()) && isNotEmpty(report.getVersion())
              && !reportDb.getName().equals(report.getName())
              && !reportDb.getVersion().equals(report.getVersion())) {
            reportQuery.checkExists(reportDb.getProjectId(), report.getName(), report.getVersion());
          }
          // Check the target cannot be modified
          ContentFilterSetting filter = report.getContentSetting().getFilter();
          ProtocolAssert.assertTrue(reportDb.getTargetType().equals(filter.getTargetType())
              && reportDb.getTargetId().equals(filter.getTargetId()), "Target cannot be modified");
          report.setTargetType(filter.getTargetType()).setTargetId(filter.getTargetId());
          // Check the report permission
          reportAuthQuery.checkModifyReportAuth(getUserId(), report.getId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(report.getId())) {
          return add(report);
        }

        ReportConverter.setReplaceInfo(report, reportDb);

        reportRepo.save(reportDb);

        activityCmd.add(toActivity(REPORT, reportDb, ActivityType.UPDATED));

        return new IdKey<Long, Object>().setId(reportDb.getId()).setKey(reportDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void generateNow(Long id) {
    new BizTemplate<Void>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = reportQuery.checkAndFind(id);
        // Check the generate report auth
        reportAuthQuery.checkGenerateReportAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        generateRecord(reportDb);

        activityCmd.add(toActivity(REPORT, reportDb, ActivityType.GEN_NOW));
        return null;
      }
    }.execute();
  }

  /**
   * Note: When the report generation fails, the scheduled job will no longer be automatically
   * generated.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void generateRecord(Report reportDb) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        Long projectId = reportDb.getProjectId();
        ContentFilterSetting filter = reportDb.getContentSetting().getFilter();
        ReportTemplate template = reportDb.getTemplate();

        ReportContent reportContent = null;
        String failureMessage = null;
        try {
          if (!isUserAction()) {
            // Transfer principal downwards
            commonQuery.setInnerPrincipal(reportDb.getTenantId(), reportDb.getCreatedBy());
          }

          reportContent = switch (template) {
            case PROJECT_PROGRESS -> reportQuery.assembleProjectProgressContent(
                filter, projectId);
            case TEST_EVALUATION -> reportQuery.assembleTestEvaluationContent(
                filter, projectId);
            case TASK_SPRINT -> reportQuery.assembleTaskSprintContent(filter, projectId);
            case TASK -> reportQuery.assembleTaskContent(filter);
            case FUNC_TESTING_PLAN -> reportQuery.assembleFuncPlanContent(filter, projectId);
            case FUNC_TESTING_CASE -> reportQuery.assembleFuncCaseContent(filter);
            case SERVICES_TESTING_RESULT ->
                reportQuery.assembleServicesTestingContent(filter, projectId);
            case APIS_TESTING_RESULT -> reportQuery.assembleApisTestingContent(filter);
            case SCENARIO_TESTING_RESULT -> reportQuery.assembleScenarioTestingContent(filter);
            case EXEC_FUNCTIONAL_RESULT, EXEC_PERFORMANCE_RESULT, EXEC_STABILITY_RESULT,
                 EXEC_CUSTOMIZATION_RESULT -> new ExecutionContent();
            default -> reportContent;
          };
        } catch (Exception e) {
          log.error("Generate report exception", e);
          failureMessage = lengthSafe(e.getMessage(), MAX_DESC_LENGTH);
          if (!isUserAction()) {
            PrincipalContext.remove();
            throw e;
          }
        }

        try {
          ReportStatus status = nonNull(failureMessage)
              ? ReportStatus.FAILURE : ReportStatus.SUCCESS;
          if (status.isSuccess()) {
            reportRecordCmd.add0(toGeneratedRecord(reportDb, reportContent));

            TimeSetting timeSetting = reportDb.getCreateTimeSetting();
            if (!timeSetting.isOnetime()) {
              // Trigger the next execution
              reportDb.setNextGenerationDate(timeSetting.getNextDate());
            }
          }

          reportDb.setStatus(status).setFailureMessage(failureMessage);
          reportRepo.save(reportDb);

          // Add report generation success event
          if (status.isSuccess()) {
            reportQuery.assembleAndSendReportGenerationSuccessfulNoticeEvent(reportDb);
          }
        } finally {
          if (!isUserAction()) {
            PrincipalContext.remove();
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId, Set<ExampleDataType> dataTypes) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertNotEmpty(dataTypes, "Example dataType is required");
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();

        // Create project report
        Project projectDb = projectQuery.checkAndFind(projectId);
        Report projectReport = toExampleDomain(projectId,
            projectDb.getName() + "-" + REPORT.getMessage(), ReportCategory.PROJECT,
            ReportTemplate.PROJECT_PROGRESS, CombinedTargetType.PROJECT, projectId);
        idKeys.add(add0(projectReport, projectDb));

        // Create task report
        if (dataTypes.contains(ExampleDataType.TASK)) {
          // Create task sprint report
          TaskSprint sprintDb = taskSprintQuery.findLeastByProjectId(projectId);
          if (nonNull(sprintDb)) {
            Report sprintReport = toExampleDomain(projectId,
                sprintDb.getName() + "-" + REPORT.getMessage(), ReportCategory.TASK,
                ReportTemplate.TASK_SPRINT, CombinedTargetType.TASK_SPRINT, sprintDb.getId());
            idKeys.add(add0(sprintReport, sprintDb));
          }

          // Create task report
          TaskInfo taskDb = taskQuery.findLeastByProjectId(projectId);
          if (nonNull(taskDb)) {
            Report taskReport = toExampleDomain(projectId,
                taskDb.getName() + "-" + REPORT.getMessage(), ReportCategory.TASK,
                ReportTemplate.TASK, CombinedTargetType.TASK, taskDb.getId());
            idKeys.add(add0(taskReport, taskDb));
          }
        }

        // Create function report
        if (dataTypes.contains(ExampleDataType.FUNC)) {
          // Create function plan report
          FuncPlan planDb = funcPlanQuery.findLeastByProjectId(projectId);
          if (nonNull(planDb)) {
            Report planReport = toExampleDomain(projectId,
                planDb.getName() + "-" + REPORT.getMessage(), ReportCategory.FUNCTIONAL,
                ReportTemplate.FUNC_TESTING_PLAN, CombinedTargetType.FUNC_PLAN, planDb.getId());
            idKeys.add(add0(planReport, planDb));
          }

          // Create function case report
          FuncCaseInfo caseDb = funcCaseQuery.findLeastByProjectId(projectId);
          if (nonNull(caseDb)) {
            Report caseReport = toExampleDomain(projectId,
                caseDb.getName() + "-" + REPORT.getMessage(), ReportCategory.FUNCTIONAL,
                ReportTemplate.FUNC_TESTING_CASE, CombinedTargetType.FUNC_CASE, caseDb.getId());
            idKeys.add(add0(caseReport, caseDb));
          }
        }

        // Create apis report
        if (dataTypes.contains(ExampleDataType.SERVICES)) {
          ApisBaseInfo apisDb = apisQuery.findLeastByProjectId(projectId);
          if (nonNull(apisDb)) {
            Report apisReport = toExampleDomain(projectId,
                apisDb.getName() + "-" + REPORT.getMessage(), ReportCategory.APIS,
                ReportTemplate.APIS_TESTING_RESULT, CombinedTargetType.API, apisDb.getId());
            idKeys.add(add0(apisReport, apisDb));
          }
        }

        // Create scenario report
        if (dataTypes.contains(ExampleDataType.SCENARIO)) {
          Scenario scenarioDb = scenarioQuery.findLeastByProjectIdAndPluginAndTypeIn(projectId,
              PLUGIN_HTTP_NAME, List.of(ScriptType.TEST_PERFORMANCE.getValue(),
                  ScriptType.TEST_FUNCTIONALITY.getValue()));
          if (nonNull(scenarioDb)) {
            Report scenarioReport = toExampleDomain(projectId,
                scenarioDb.getName() + "-" + REPORT.getMessage(), ReportCategory.SCENARIO,
                ReportTemplate.SCENARIO_TESTING_RESULT, CombinedTargetType.SCENARIO,
                scenarioDb.getId());
            idKeys.add(add0(scenarioReport, scenarioDb));
          }
        }

        // Create execution report ? Starting the test run may take a long time !!!
        // Do nothing!
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<Report> reportsDb;

      @Override
      protected void checkParams() {
        reportsDb = reportQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        reportRepo.deleteByIdIn(ids);
        reportAuthCmd.deleteByReportId(ids);

        activityCmd.addAll(toActivities(REPORT, reportsDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<Report, Long> getRepository() {
    return reportRepo;
  }
}
