package cloud.xcan.angus.core.tester.application.query.report;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.report.ReportResourcesCount;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.record.content.ApisTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.FuncCaseContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.FuncPlanContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.TaskContent;
import cloud.xcan.angus.core.tester.domain.report.record.content.TaskSprintContent;
import cloud.xcan.angus.core.tester.domain.setting.ContentFilterSetting;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ReportQuery {

  Report detail(Long id);

  String shareToken(Long id);

  Object content(Long id);

  Object content(Long id, String token, Long recordId);

  Page<ReportInfo> find(GenericSpecification<ReportInfo> spec, PageRequest pageable);

  ReportResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  boolean isAuthCtrl(Long reportId);

  void checkFilters(ReportTemplate template, ContentFilterSetting filter);

  void checkExists(Long projectId, String name, String version);

  ActivityResource checkAndFindResource(ReportTemplate template, Long projectId,
      CombinedTargetType targetType, Long targetId);

  void checkQuota();

  Report checkAndFind(Long reportId);

  ReportInfo checkAndFindInfo(Long id);

  List<Report> checkAndFind(Collection<Long> ids);

  void setReportInfoCurrentAuths(List<ReportInfo> reports);

  ScenarioTestingContent assembleScenarioTestingContent(ContentFilterSetting filter);

  ApisTestingContent assembleApisTestingContent(ContentFilterSetting filter);

  ServicesTestingContent assembleServicesTestingContent(ContentFilterSetting filter, Long projectId);

  FuncCaseContent assembleFuncCaseContent(ContentFilterSetting filter);

  FuncPlanContent assembleFuncPlanContent(ContentFilterSetting filter, Long projectId);

  TaskContent assembleTaskContent(ContentFilterSetting filter);

  TaskSprintContent assembleTaskSprintContent(ContentFilterSetting filter, Long projectId);

  ProjectProgressContent assembleProjectProgressContent(ContentFilterSetting filter, Long projectId);

  TestEvaluationResult assembleTestEvaluationContent(ContentFilterSetting filter, Long projectId);

  void assembleAndSendReportGenerationSuccessfulNoticeEvent(Report reportDb);

}
