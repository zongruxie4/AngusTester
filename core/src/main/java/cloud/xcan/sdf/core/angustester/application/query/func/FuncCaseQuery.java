package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.BackloggedOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.BurnDownChartOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.CoreKpiOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncTesterCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncTesterProgressCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.GrowthTrendOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.LeadTimeOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.OverdueAssessmentOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.ProgressOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.RecentDeliveryOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.ResourceCreationOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.ReviewEfficiencyOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.TestingEfficiencyOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.UnplannedWorkOverview;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.WorkloadOverview;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncPlanWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncProjectWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncTesterWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.spec.annotations.NonNullable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncCaseQuery {

  FuncCase detail(Long id);

  Page<FuncCaseInfo> list(GenericSpecification<FuncCaseInfo> spec, Pageable pageable);

  List<FuncCaseInfo> notAssociatedCaseInTask(Long id, Long moduleId);

  List<FuncCaseInfo> notAssociatedCaseInCase(Long caseId, Long moduleId);

  FuncLastResourceCreationCount creationResourcesStatistics(Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinPlan, boolean joinReview, boolean joinBaseline);

  FuncCaseCount countStatistics(Set<SearchCriteria> criterias);

  List<FuncTesterCount> testerSummaryStatistics(Long projectId, Long planId);

  List<FuncTesterProgressCount> testerProgressStatistics(Long projectId, Long planId);

  FuncProjectWorkSummary projectWorkStatistics(Long projectId);

  FuncPlanWorkSummary planWorkStatistics(Long planId);

  FuncTesterWorkSummary testerWorkStatistics(Long projectId, Long planId, Long userId);

  ProgressOverview progress(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  BurnDownChartOverview burndownChart(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  WorkloadOverview workload(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  OverdueAssessmentOverview overdueAssessment(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  TestingEfficiencyOverview testingEfficiency(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  CoreKpiOverview coreKpi(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  ReviewEfficiencyOverview reviewEfficiency(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime startTime, LocalDateTime endTime, boolean containsUserAnalysis,
      boolean containsDataDetail);

  BackloggedOverview backloggedWork(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  RecentDeliveryOverview recentDelivery(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  LeadTimeOverview leadTime(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  UnplannedWorkOverview unplannedWork(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail);

  GrowthTrendOverview growthTrend(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime startTime, LocalDateTime endTime, boolean joinTesterDetail,
      boolean joinDataDetail);

  ResourceCreationOverview resourceCreation(
      @NonNullable Long projectId, Long planId, AuthObjectType creatorOrgType,
      Long creatorOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCreatorDetail, boolean joinDataDetail);

  List<FuncCaseEfficiencySummary> getCaseEfficiencySummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType testerOrgType,
      Long testerOrgId);

  AbstractOverview assembleCaseAnalysisSnapshot(Analysis analysis);

  AbstractOverview parseCaseAnalysisSnapshot(String template0, String snapshot);

  void checkCaseQuota(int inc, Long planId);

  Map<String, List<FuncCaseInfo>> checkAndFindByPlanAndName(Long projectId, Set<String> caseNames);

  void checkAddCaseNameExists(FuncPlan planDb, List<FuncCase> cases);

  void checkAndSafeUpdateNameExists(FuncPlan planDb, List<FuncCase> cases);

  FuncCase checkAndFind(Long id);

  FuncCaseInfo checkAndFindInfo(Long id);

  List<FuncCase> checkAndFind(Collection<Long> ids);

  List<FuncCaseInfo> checkAndFindInfo(Collection<Long> ids);

  void checkReviewPassed(List<FuncCase> cases);

  void checkCanReview(List<FuncCase> casesDb);

  void checkInfoCanReview(List<FuncCaseInfo> cases);

  boolean hasModifyAttachments(List<Attachment> attachments, FuncCase caseDb);

  void checkUpdateNameExists(Long planId, String name, Long id);

  void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> cases);

  void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> cases);

  void setSafeCloneName(FuncCase funcCase);

  void assembleAndSendModifyNoticeEvent(List<FuncCaseInfo> casesDb, List<Activity> activities);

  void assembleAndSendModifyNoticeEvent(FuncCaseInfo caseDb, Activity activity);

  void assembleAndSendModifyNoticeEvent(FuncCase caseDb, Activity activity);

  void assembleAndSendModifyTesterNoticeEvent(FuncCase caseDb);

}
