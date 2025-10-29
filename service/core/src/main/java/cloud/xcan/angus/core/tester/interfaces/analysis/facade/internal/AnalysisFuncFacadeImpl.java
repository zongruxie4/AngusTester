package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisFuncAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisFuncAssembler.toCaseStatisticsExportResource;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.test.cases.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncTesterCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncTesterProgressCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncPlanWorkSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncTesterWorkSummary;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.task.count.TesterSubmittedBugOverview;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisFuncFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.CaseTesterWorkStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncAnalysisDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncCaseSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncCreatorStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncTesterSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncTesterWorkStatisticsDto;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AnalysisFuncFacadeImpl implements AnalysisFuncFacade {

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private TaskQuery taskQuery;

  @Override
  public FuncLastResourceCreationCount creationResourcesStatistics(FuncCreatorStatisticsDto dto) {
    return funcCaseQuery.creationResourcesStatistics(dto.getProjectId(), dto.getPlanId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), true, true, true);
  }

  @Override
  public FuncCaseCount countStatistics(FuncCaseSummaryStatisticsDto dto) {
    return funcCaseQuery.countStatistics(getSearchCriteria(dto));
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      FuncCaseSummaryStatisticsDto dto, HttpServletResponse response) {
    FuncCaseCount count = funcCaseQuery.countStatistics(getSearchCriteria(dto));
    String fileName = "CaseCountStatisticsExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName, 0, toCaseStatisticsExportResource(count, fileName));
  }

  @Override
  public List<FuncTesterCount> testerSummaryStatistics(FuncTesterSummaryStatisticsDto dto) {
    return funcCaseQuery.testerSummaryStatistics(dto.getProjectId(), dto.getPlanId());
  }

  @Override
  public List<FuncTesterProgressCount> testerProgressStatistics(
      FuncTesterSummaryStatisticsDto dto) {
    return funcCaseQuery.testerProgressStatistics(dto.getProjectId(), dto.getPlanId());
  }

  @Override
  public FuncProjectWorkSummary projectWorkStatistics(Long projectId) {
    return funcCaseQuery.projectWorkStatistics(projectId);
  }

  @Override
  public FuncPlanWorkSummary planWorkStatistics(Long planId) {
    return funcCaseQuery.planWorkStatistics(planId);
  }

  @Override
  public FuncTesterWorkSummary testerWorkStatistics(FuncTesterWorkStatisticsDto dto) {
    return funcCaseQuery.testerWorkStatistics(dto.getProjectId(), dto.getPlanId(), dto.getUserId());
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> projectBurndownChart(Long projectId) {
    return funcCaseQuery.burndownChart(projectId, null, null, null,
        null, null, false, false).getTotalBurnDownCharts();
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> planBurndownChart(Long planId) {
    return funcCaseQuery.burndownChart(null, planId, null, null,
        null, null, false, false).getTotalBurnDownCharts();
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> testerBurndownChart(
      CaseTesterWorkStatisticsDto dto) {
    return funcCaseQuery.burndownChart(dto.getProjectId(), dto.getPlanId(),
        AuthObjectType.USER, dto.getUserId(), null, null, false, false).getTotalBurnDownCharts();
  }

  @Override
  public ProgressOverview progress(FuncAnalysisDto dto) {
    return funcCaseQuery.progress(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public BurnDownChartOverview burndownChart(FuncAnalysisDto dto) {
    return funcCaseQuery.burndownChart(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public WorkloadOverview workload(FuncAnalysisDto dto) {
    return funcCaseQuery.workload(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public OverdueAssessmentOverview overdueAssessment(FuncAnalysisDto dto) {
    return funcCaseQuery.overdueAssessment(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public TesterSubmittedBugOverview submittedBug(FuncAnalysisDto dto) {
    return taskQuery.submittedBug(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public TestingEfficiencyOverview testingEfficiency(FuncAnalysisDto dto) {
    return funcCaseQuery.testingEfficiency(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public CoreKpiOverview coreKpi(FuncAnalysisDto dto) {
    return funcCaseQuery.coreKpi(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public ReviewEfficiencyOverview reviewEfficiency(FuncAnalysisDto dto) {
    return funcCaseQuery.reviewEfficiency(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public BackloggedOverview backloggedWork(FuncAnalysisDto dto) {
    return funcCaseQuery.backloggedWork(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public RecentDeliveryOverview recentDelivery(FuncAnalysisDto dto) {
    return funcCaseQuery.recentDelivery(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public LeadTimeOverview leadTime(FuncAnalysisDto dto) {
    return funcCaseQuery.leadTime(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public UnplannedWorkOverview unplannedWork(FuncAnalysisDto dto) {
    return funcCaseQuery.unplannedWork(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public GrowthTrendOverview growthTrend(FuncAnalysisDto dto) {
    return funcCaseQuery.growthTrend(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

  @Override
  public ResourceCreationOverview resourceCreation(FuncAnalysisDto dto) {
    return funcCaseQuery.resourceCreation(dto.getProjectId(), dto.getPlanId(), dto.getOrgType(),
        dto.getOrgId(), dto.getStartTime(), dto.getEndTime(), dto.isContainsUserAnalysis(),
        dto.isContainsDataDetail());
  }

}
