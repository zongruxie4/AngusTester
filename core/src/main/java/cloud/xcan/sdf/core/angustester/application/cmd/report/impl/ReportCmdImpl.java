package cloud.xcan.sdf.core.angustester.application.cmd.report.impl;

import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.REPORT;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ReportConverter.toExampleDomain;
import static cloud.xcan.sdf.core.angustester.application.converter.ReportConverter.toGeneratedRecord;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.lengthSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportAuthCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportRecordCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ReportConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportRepo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportStatus;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ExecutionContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ReportContent;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentFilterSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
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
        resourceDb = reportQuery.checkAndFindResource(report.getProjectId(),
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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        Long projectId = reportDb.getProjectId();
        ContentFilterSetting filter = reportDb.getContentSetting().getFilter();
        ReportTemplate template = reportDb.getTemplate();
        OrgAndDateFilterDto orgAndDateFilter = new OrgAndDateFilterDto()
            .setCreatorObjectType(filter.getCreatorObjectType())
            .setCreatorObjectId(filter.getCreatorObjectId())
            .setCreatedDateStart(filter.getCreatedDateStart())
            .setCreatedDateEnd(filter.getCreatedDateEnd());

        ReportContent reportContent = null;
        String failureMessage = null;
        try {
          if (!isUserAction()) {
            // Transfer principal downwards
            commonQuery.setInnerPrincipal(reportDb.getTenantId(), reportDb.getCreatedBy());
          }

          switch (template) {
            case PROJECT_PROGRESS:
              reportContent = reportQuery.assembleProjectProgressContent(filter, projectId,
                  orgAndDateFilter);
              break;
            case TASK_SPRINT:
              reportContent = reportQuery.assembleTaskSprintContent(filter, projectId);
              break;
            case TASK:
              reportContent = reportQuery.assembleTaskContent(filter);
              break;
            case FUNC_TESTING_PLAN:
              reportContent = reportQuery.assembleFuncPlanContent(filter, projectId);
              break;
            case FUNC_TESTING_CASE:
              reportContent = reportQuery.assembleFuncCaseContent(filter);
              break;
            case SERVICES_TESTING_RESULT:
              reportContent = reportQuery.assembleServicesTestingContent(filter, projectId,
                  orgAndDateFilter);
              break;
            case APIS_TESTING_RESULT:
              reportContent = reportQuery.assembleApisTestingContent(filter);
              break;
            case SCENARIO_TESTING_RESULT:
              reportContent = reportQuery.assembleScenarioTestingContent(filter);
              break;
            case EXEC_FUNCTIONAL_RESULT:
            case EXEC_PERFORMANCE_RESULT:
            case EXEC_STABILITY_RESULT:
            case EXEC_CUSTOMIZATION_RESULT:
              reportContent = new ExecutionContent();
              break;
          }
        } catch (Exception e) {
          log.error("Generate report exception", e);
          failureMessage = lengthSafe(e.getMessage(), DEFAULT_DESC_LENGTH);
          if (!isUserAction()) {
            PrincipalContext.remove();
            throw e;
          }
        }

        try {
          ReportStatus status = nonNull(failureMessage) ? ReportStatus.FAILURE
              : ReportStatus.SUCCESS;
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
