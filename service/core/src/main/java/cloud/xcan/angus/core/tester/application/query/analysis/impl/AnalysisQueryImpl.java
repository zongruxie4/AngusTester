package cloud.xcan.angus.core.tester.application.query.analysis.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.AnalysisConverter.getAnalysisCreatorResourcesFilter;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisQuery;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisSnapshotQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisCaseTemplate;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisRepo;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTaskTemplate;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.angus.core.tester.domain.issue.count.AbstractOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BugOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.FailureAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ProcessingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.TesterSubmittedBugOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadOverview;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import jakarta.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AnalysisQueryImpl implements AnalysisQuery {

  @Resource
  private AnalysisRepo analysisRepo;

  @Resource
  private AnalysisSnapshotQuery analysisSnapshotQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Analysis detail(Long id) {
    return new BizTemplate<Analysis>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        analysisDb = checkAndFind(id);
      }

      @Override
      protected Analysis process() {
        AnalysisSnapshot snapshot = null;
        if (analysisDb.getDatasource().isSnapshotData()) {
          snapshot = analysisSnapshotQuery.checkAndFindByAnalysisId(analysisDb.getId());
          analysisDb.setDataObj(parseSnapshotDataString(analysisDb, snapshot.getData()));
        }
        if (isNull(snapshot)) {
          analysisDb.setDataObj(getSnapshotData(analysisDb));
        }
        return analysisDb;
      }
    }.execute();
  }

  @Override
  public Page<Analysis> find(GenericSpecification<Analysis> spec, PageRequest pageable) {
    return new BizTemplate<Page<Analysis>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<Analysis> process() {
        return analysisRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public File overviewExport(Long id) {
    return new BizTemplate<File>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        analysisDb = detail(id);
      }

      @SneakyThrows
      @Override
      protected File process() {
        String fileName = analysisDb.getName() + "-" + System.currentTimeMillis() + ".xlsx";
        String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_ANALYSIS_DIR
            + getTenantId() + File.separator + fileName;
        File file = new File(filePath);
        FileUtils.forceMkdirParent(file);
        analysisDb.getDataObj().writeExcelFile(file);
        return file;
      }
    }.execute();
  }

  @Override
  public void checkExits(Long projectId, String name, AnalysisResource resource) {
    long count = analysisRepo.countByProjectIdAndNameAndResource(projectId, name, resource);
    if (count > 0) {
      throw ResourceExisted.of(name, "Analysis");
    }
  }

  @Override
  public Analysis checkAndFind(Long id) {
    return analysisRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Analysis"));
  }

  @Override
  public List<Analysis> checkAndFind(Collection<Long> ids) {
    List<Analysis> analyses = analysisRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(analyses), ids.iterator().next(), "Report");
    if (ids.size() != analyses.size()) {
      for (Analysis analysis : analyses) {
        assertResourceNotFound(ids.contains(analysis.getId()), analysis.getId(), "Report");
      }
    }
    return analyses;
  }

  @Override
  public AbstractOverview getSnapshotData(Analysis analysis) {
    AbstractOverview data = null;
    if (analysis.getResource().isTask()) {
      data = assembleTaskAnalysisSnapshot(analysis);
    }
    if (analysis.getResource().isCase()) {
      data = assembleCaseAnalysisSnapshot(analysis);
    }
    return data;
  }

  @Override
  public AbstractOverview assembleTaskAnalysisSnapshot(Analysis analysis) {
    AnalysisTaskTemplate template = AnalysisTaskTemplate.valueOf(analysis.getTemplate());
    return switch (template) {
      case PROGRESS -> taskQuery.progress(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case BURNDOWN -> taskQuery.burndownChart(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case WORKLOAD -> taskQuery.workload(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case OVERDUE_ASSESSMENT ->
          taskQuery.overdueAssessment(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case BUGS -> taskQuery.bug(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case HANDLING_EFFICIENCY ->
          taskQuery.processingEfficiency(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case CORE_KPI -> taskQuery.coreKpi(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case FAILURES -> taskQuery.failureAssessment(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case BACKLOG_TASKS -> taskQuery.backloggedWork(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case RECENT_DELIVERY ->
          taskQuery.recentDelivery(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case LEAD_TIME -> taskQuery.leadTime(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case UNPLANNED_TASKS -> taskQuery.unplannedWork(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case TASK_GROWTH_TREND -> taskQuery.growthTrend(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case RESOURCE_CREATION ->
          taskQuery.resourceCreation(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
    };
  }

  @Override
  public AbstractOverview assembleCaseAnalysisSnapshot(Analysis analysis) {
    AnalysisCaseTemplate template = AnalysisCaseTemplate.valueOf(analysis.getTemplate());
    return switch (template) {
      case PROGRESS -> funcCaseQuery.progress(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case BURNDOWN -> funcCaseQuery.burndownChart(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case WORKLOAD -> funcCaseQuery.workload(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case OVERDUE_ASSESSMENT ->
          funcCaseQuery.overdueAssessment(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case SUBMITTED_BUGS -> taskQuery.submittedBug(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case TESTING_EFFICIENCY ->
          funcCaseQuery.testingEfficiency(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case CORE_KPI -> funcCaseQuery.coreKpi(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case REVIEW_EFFICIENCY ->
          funcCaseQuery.reviewEfficiency(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case BACKLOG_CASES ->
          funcCaseQuery.backloggedWork(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case RECENT_DELIVERY ->
          funcCaseQuery.recentDelivery(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case LEAD_TIME -> funcCaseQuery.leadTime(analysis.getProjectId(), analysis.getPlanId(),
          analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
          analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
          analysis.getContainsDataDetail());
      case UNPLANNED_CASES ->
          funcCaseQuery.unplannedWork(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case CASE_GROWTH_TREND ->
          funcCaseQuery.growthTrend(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
      case RESOURCE_CREATION ->
          funcCaseQuery.resourceCreation(analysis.getProjectId(), analysis.getPlanId(),
              analysis.getOrgType(), analysis.getOrgId(), analysis.getCalcStartTime(),
              analysis.getCalcEndTime(), analysis.getContainsUserAnalysis(),
              analysis.getContainsDataDetail());
    };
  }

  @Override
  public String getSnapshotDataString(Analysis analysis) {
    return JsonUtils.toJson(getSnapshotData(analysis));
  }

  @Override
  public AbstractOverview parseSnapshotDataString(Analysis analysis, String snapshot) {
    if (isEmpty(snapshot)) {
      return null;
    }
    AbstractOverview data = null;
    if (analysis.getResource().isTask()) {
      data = parseTaskAnalysisSnapshot(analysis.getTemplate(), snapshot);
    }
    if (analysis.getResource().isCase()) {
      data = parseCaseAnalysisSnapshot(analysis.getTemplate(), snapshot);
    }
    return data;
  }

  @Override
  @SneakyThrows
  public AbstractOverview parseTaskAnalysisSnapshot(String template0, String snapshot) {
    AnalysisTaskTemplate template = AnalysisTaskTemplate.valueOf(template0);
    return switch (template) {
      case PROGRESS -> JsonUtils.convert(snapshot, ProgressOverview.class);
      case BURNDOWN -> JsonUtils.convert(snapshot, BurnDownChartOverview.class);
      case WORKLOAD -> JsonUtils.convert(snapshot, WorkloadOverview.class);
      case OVERDUE_ASSESSMENT -> JsonUtils.convert(snapshot, OverdueAssessmentOverview.class);
      case BUGS -> JsonUtils.convert(snapshot, BugOverview.class);
      case HANDLING_EFFICIENCY -> JsonUtils.convert(snapshot, ProcessingEfficiencyOverview.class);
      case CORE_KPI -> JsonUtils.convert(snapshot, CoreKpiOverview.class);
      case FAILURES -> JsonUtils.convert(snapshot, FailureAssessmentOverview.class);
      case BACKLOG_TASKS -> JsonUtils.convert(snapshot, BackloggedOverview.class);
      case RECENT_DELIVERY -> JsonUtils.convert(snapshot, RecentDeliveryOverview.class);
      case LEAD_TIME -> JsonUtils.convert(snapshot, LeadTimeOverview.class);
      case UNPLANNED_TASKS -> JsonUtils.convert(snapshot, UnplannedWorkOverview.class);
      case TASK_GROWTH_TREND -> JsonUtils.convert(snapshot, GrowthTrendOverview.class);
      case RESOURCE_CREATION -> JsonUtils.convert(snapshot, ResourceCreationOverview.class);
    };
  }

  @SneakyThrows
  @Override
  public AbstractOverview parseCaseAnalysisSnapshot(String template0, String snapshot) {
    AnalysisCaseTemplate template = AnalysisCaseTemplate.valueOf(template0);
    return switch (template) {
      case PROGRESS -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.ProgressOverview.class);
      case BURNDOWN -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.BurnDownChartOverview.class);
      case WORKLOAD -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.WorkloadOverview.class);
      case OVERDUE_ASSESSMENT -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.OverdueAssessmentOverview.class);
      case SUBMITTED_BUGS -> JsonUtils.convert(snapshot, TesterSubmittedBugOverview.class);
      case TESTING_EFFICIENCY -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyOverview.class);
      case CORE_KPI -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiOverview.class);
      case REVIEW_EFFICIENCY -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyOverview.class);
      case BACKLOG_CASES -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.BackloggedOverview.class);
      case RECENT_DELIVERY -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.RecentDeliveryOverview.class);
      case LEAD_TIME -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.LeadTimeOverview.class);
      case UNPLANNED_CASES -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.UnplannedWorkOverview.class);
      case CASE_GROWTH_TREND -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendOverview.class);
      case RESOURCE_CREATION -> JsonUtils.convert(snapshot,
          cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationOverview.class);
    };
  }

  @Override
  public List<Analysis> getAnalysisCreatedSummaries(AnalysisResource resource, Long projectId,
      Long sprintId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      AuthObjectType creatorOrgType, Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getAnalysisCreatorResourcesFilter(
        resource, projectId, sprintId, createdDateStart, createdDateEnd, creatorIds);
    return analysisRepo.findAllByFilters(allFilters);
  }

}
