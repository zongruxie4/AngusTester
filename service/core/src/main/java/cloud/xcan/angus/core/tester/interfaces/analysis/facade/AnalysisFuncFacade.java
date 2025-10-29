package cloud.xcan.angus.core.tester.interfaces.analysis.facade;


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
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.CaseTesterWorkStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncAnalysisDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncCaseSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncCreatorStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncTesterSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncTesterWorkStatisticsDto;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AnalysisFuncFacade {

  FuncLastResourceCreationCount creationResourcesStatistics(FuncCreatorStatisticsDto dto);

  FuncCaseCount countStatistics(FuncCaseSummaryStatisticsDto dto);

  ResponseEntity<Resource> countStatisticsExport(FuncCaseSummaryStatisticsDto dto,
      HttpServletResponse response);

  List<FuncTesterCount> testerSummaryStatistics(FuncTesterSummaryStatisticsDto dto);

  List<FuncTesterProgressCount> testerProgressStatistics(FuncTesterSummaryStatisticsDto dto);

  FuncProjectWorkSummary projectWorkStatistics(Long projectId);

  FuncPlanWorkSummary planWorkStatistics(Long planId);

  FuncTesterWorkSummary testerWorkStatistics(FuncTesterWorkStatisticsDto dto);

  Map<BurnDownResourceType, BurnDownChartCount> projectBurndownChart(Long projectId);

  Map<BurnDownResourceType, BurnDownChartCount> planBurndownChart(Long planId);

  Map<BurnDownResourceType, BurnDownChartCount> testerBurndownChart(
      CaseTesterWorkStatisticsDto dto);

  ProgressOverview progress(FuncAnalysisDto dto);

  BurnDownChartOverview burndownChart(FuncAnalysisDto dto);

  WorkloadOverview workload(FuncAnalysisDto dto);

  OverdueAssessmentOverview overdueAssessment(FuncAnalysisDto dto);

  TesterSubmittedBugOverview submittedBug(FuncAnalysisDto dto);

  TestingEfficiencyOverview testingEfficiency(FuncAnalysisDto dto);

  CoreKpiOverview coreKpi(FuncAnalysisDto dto);

  ReviewEfficiencyOverview reviewEfficiency(FuncAnalysisDto dto);

  BackloggedOverview backloggedWork(FuncAnalysisDto dto);

  RecentDeliveryOverview recentDelivery(FuncAnalysisDto dto);

  LeadTimeOverview leadTime(FuncAnalysisDto dto);

  UnplannedWorkOverview unplannedWork(FuncAnalysisDto dto);

  GrowthTrendOverview growthTrend(FuncAnalysisDto dto);

  ResourceCreationOverview resourceCreation(FuncAnalysisDto dto);

}
