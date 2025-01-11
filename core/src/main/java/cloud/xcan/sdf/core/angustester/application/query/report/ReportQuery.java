package cloud.xcan.sdf.core.angustester.application.query.report;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportResourcesCount;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ApisTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncCaseContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncPlanContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskSprintContent;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentFilterSetting;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
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

  ActivityResource checkAndFindResource(Long projectId, CombinedTargetType targetType,
      Long targetId);

  void checkQuota();

  Report checkAndFind(Long reportId);

  ReportInfo checkAndFindInfo(Long id);

  List<Report> checkAndFind(Collection<Long> ids);

  void setReportInfoCurrentAuths(List<ReportInfo> reports);


  @NotNull
  ScenarioTestingContent assembleScenarioTestingContent(
      ContentFilterSetting filter);

  @NotNull
  ApisTestingContent assembleApisTestingContent(ContentFilterSetting filter);

  @NotNull
  ServicesTestingContent assembleServicesTestingContent(
      ContentFilterSetting filter, Long projectId, OrgAndDateFilterDto orgAndDateFilterDto);

  @NotNull
  FuncCaseContent assembleFuncCaseContent(ContentFilterSetting filter);

  @NotNull
  FuncPlanContent assembleFuncPlanContent(ContentFilterSetting filter,
      Long projectId);

  @NotNull
  TaskContent assembleTaskContent(ContentFilterSetting filter);

  @NotNull
  TaskSprintContent assembleTaskSprintContent(ContentFilterSetting filter,
      Long projectId);

  @NotNull
  ProjectProgressContent assembleProjectProgressContent(
      ContentFilterSetting filter, Long projectId, OrgAndDateFilterDto orgAndDateFilterDto);

  void assembleAndSendReportGenerationSuccessfulNoticeEvent(Report reportDb);
}
