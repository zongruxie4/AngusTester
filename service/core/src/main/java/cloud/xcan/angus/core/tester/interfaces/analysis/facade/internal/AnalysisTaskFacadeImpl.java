package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisTaskAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisTaskAssembler.toTaskStatisticsExport;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BugOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartCount;
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
import cloud.xcan.angus.core.tester.domain.issue.count.TaskAssigneeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskAssigneeProgressCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskAssigneeWorkSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSprintWorkSummary;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisTaskFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAnalysisDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeWorkStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskCreatorStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskSummaryStatisticsDto;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AnalysisTaskFacadeImpl implements AnalysisTaskFacade {

  @Resource
  private TaskQuery taskQuery;

  @Override
  public TaskLastResourceCreationCount creationResourcesStatistics(TaskCreatorStatisticsDto dto) {
    return taskQuery.creationResourcesStatistics(dto.getProjectId(), dto.getSprintId(),
        dto.getCreatorObjectType(), dto.getCreatorObjectId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), true, true);
  }

  @Override
  public TaskCount countStatistics(TaskSummaryStatisticsDto dto) {
    return taskQuery.countStatistics(getSearchCriteria(dto));
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      TaskSummaryStatisticsDto dto, HttpServletResponse response) {
    TaskCount count = taskQuery.countStatistics(getSearchCriteria(dto));
    String fileName = "TaskCountStatisticsExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName, 0, toTaskStatisticsExport(count, fileName));
  }

  @Override
  public List<TaskAssigneeCount> assigneeSummaryStatistics(TaskAssigneeSummaryStatisticsDto dto) {
    return taskQuery.assigneeSummaryStatistics(dto.getProjectId(), dto.getSprintId());
  }

  @Override
  public List<TaskAssigneeProgressCount> assigneeProgressStatistics(
      TaskAssigneeSummaryStatisticsDto dto) {
    return taskQuery.assigneeProgressStatistics(dto.getProjectId(), dto.getSprintId());
  }

  @Override
  public TaskProjectWorkSummary projectWorkStatistics(Long projectId) {
    return taskQuery.projectWorkStatistics(projectId);
  }

  @Override
  public TaskSprintWorkSummary sprintWorkStatistics(Long sprintId) {
    return taskQuery.sprintWorkStatistics(sprintId);
  }

  @Override
  public TaskAssigneeWorkSummary assigneeWorkStatistics(TaskAssigneeWorkStatisticsDto dto) {
    return taskQuery.assigneeWorkStatistics(dto.getProjectId(), dto.getSprintId(), dto.getUserId());
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> projectBurndownChart(Long projectId) {
    return taskQuery.burndownChart(projectId, null, null, null, null,
        null, false, false).getTotalBurnDownCharts();
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> sprintBurndownChart(Long sprintId) {
    return taskQuery.burndownChart(null, sprintId, null, null, null, null,
        false, false).getTotalBurnDownCharts();
  }

  @Override
  public Map<BurnDownResourceType, BurnDownChartCount> assigneeBurndownChart(
      TaskAssigneeWorkStatisticsDto dto) {
    return taskQuery.burndownChart(dto.getProjectId(), dto.getSprintId(),
        AuthObjectType.USER, dto.getUserId(), null, null, false, false).getTotalBurnDownCharts();
  }

  @Override
  public ProgressOverview progress(TaskAnalysisDto dto) {
    return taskQuery.progress(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public BurnDownChartOverview burndownChart(TaskAnalysisDto dto) {
    return taskQuery.burndownChart(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public WorkloadOverview workload(TaskAnalysisDto dto) {
    return taskQuery.workload(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public OverdueAssessmentOverview overdueAssessment(TaskAnalysisDto dto) {
    return taskQuery.overdueAssessment(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public BugOverview bug(TaskAnalysisDto dto) {
    return taskQuery.bug(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public ProcessingEfficiencyOverview processingEfficiency(TaskAnalysisDto dto) {
    return taskQuery.processingEfficiency(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public CoreKpiOverview coreKpi(TaskAnalysisDto dto) {
    return taskQuery.coreKpi(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public FailureAssessmentOverview failureAssessment(TaskAnalysisDto dto) {
    return taskQuery.failureAssessment(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public BackloggedOverview backloggedWork(TaskAnalysisDto dto) {
    return taskQuery.backloggedWork(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public RecentDeliveryOverview recentDelivery(TaskAnalysisDto dto) {
    return taskQuery.recentDelivery(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public LeadTimeOverview leadTime(TaskAnalysisDto dto) {
    return taskQuery.leadTime(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public UnplannedWorkOverview unplannedWork(TaskAnalysisDto dto) {
    return taskQuery.unplannedWork(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public GrowthTrendOverview growthTrend(TaskAnalysisDto dto) {
    return taskQuery.growthTrend(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

  @Override
  public ResourceCreationOverview resourceCreation(TaskAnalysisDto dto) {
    return taskQuery.resourceCreation(dto.getProjectId(), dto.getSprintId(),
        dto.getOrgType(), dto.getOrgId(), dto.getStartTime(), dto.getEndTime(),
        dto.isContainsUserAnalysis(), dto.isContainsDataDetail());
  }

}
