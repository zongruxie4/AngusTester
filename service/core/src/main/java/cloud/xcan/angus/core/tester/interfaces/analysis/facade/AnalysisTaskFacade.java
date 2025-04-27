package cloud.xcan.angus.core.tester.interfaces.analysis.facade;


import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.task.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.task.count.BugOverview;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.task.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.task.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.task.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.task.count.ProcessingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.task.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.task.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.task.count.TaskAssigneeCount;
import cloud.xcan.angus.core.tester.domain.task.count.TaskAssigneeProgressCount;
import cloud.xcan.angus.core.tester.domain.task.count.TaskCount;
import cloud.xcan.angus.core.tester.domain.task.count.TaskLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskAssigneeWorkSummary;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskSprintWorkSummary;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAnalysisDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeWorkStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskCreatorStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskSummaryStatisticsDto;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AnalysisTaskFacade {

  TaskLastResourceCreationCount creationResourcesStatistics(TaskCreatorStatisticsDto dto);

  TaskCount countStatistics(TaskSummaryStatisticsDto dto);

  ResponseEntity<Resource> countStatisticsExport(TaskSummaryStatisticsDto dto,
      HttpServletResponse response);

  List<TaskAssigneeCount> assigneeSummaryStatistics(TaskAssigneeSummaryStatisticsDto dto);

  List<TaskAssigneeProgressCount> assigneeProgressStatistics(TaskAssigneeSummaryStatisticsDto dto);

  TaskProjectWorkSummary projectWorkStatistics(Long projectId);

  TaskSprintWorkSummary sprintWorkStatistics(Long planId);

  TaskAssigneeWorkSummary assigneeWorkStatistics(TaskAssigneeWorkStatisticsDto dto);

  Map<BurnDownResourceType, BurnDownChartCount> projectBurndownChart(Long projectId);

  Map<BurnDownResourceType, BurnDownChartCount> sprintBurndownChart(Long sprintId);

  Map<BurnDownResourceType, BurnDownChartCount> assigneeBurndownChart(TaskAssigneeWorkStatisticsDto dto);

  ProgressOverview progress(TaskAnalysisDto dto);

  BurnDownChartOverview burndownChart(TaskAnalysisDto dto);

  WorkloadOverview workload(TaskAnalysisDto dto);

  OverdueAssessmentOverview overdueAssessment(TaskAnalysisDto dto);

  BugOverview bug(TaskAnalysisDto dto);

  ProcessingEfficiencyOverview processingEfficiency(TaskAnalysisDto dto);

  CoreKpiOverview coreKpi(TaskAnalysisDto dto);

  FailureAssessmentOverview failureAssessment(TaskAnalysisDto dto);

  BackloggedOverview backloggedWork(TaskAnalysisDto dto);

  RecentDeliveryOverview recentDelivery(TaskAnalysisDto dto);

  LeadTimeOverview leadTime(TaskAnalysisDto dto);

  UnplannedWorkOverview unplannedWork(TaskAnalysisDto dto);

  GrowthTrendOverview growthTrend(TaskAnalysisDto dto);

  ResourceCreationOverview resourceCreation(TaskAnalysisDto dto);
}
