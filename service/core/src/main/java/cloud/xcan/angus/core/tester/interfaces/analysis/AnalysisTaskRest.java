package cloud.xcan.angus.core.tester.interfaces.analysis;


import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
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
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisTaskFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAnalysisDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeSummaryStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskAssigneeWorkStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskCreatorStatisticsDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.TaskSummaryStatisticsDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis Task", description = "Task Management Analytics - Analytics for R&D task management including sprints, backlogs, tasks, bugs, progress tracking, efficiency metrics, and performance insights")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/task")
public class AnalysisTaskRest {

  @Resource
  private AnalysisTaskFacade analysisAngusTaskFacade;

  @Operation(summary = "Task creation resource statistics",
      description = "Retrieve comprehensive statistics on task creation patterns including associated modules, plans, and tags",
      operationId = "analysis:task:resources:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task creation statistics retrieved successfully")})
  @GetMapping("/resources/count")
  public ApiLocaleResult<TaskLastResourceCreationCount> creationResourcesStatistics(
      @Valid @ParameterObject TaskCreatorStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.creationResourcesStatistics(dto));
  }

  @Operation(summary = "Task count statistics",
      description = "Query and analyze task counts with comprehensive filtering and categorization metrics",
      operationId = "analysis:task:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task count statistics retrieved successfully")})
  @GetMapping("/count")
  public ApiLocaleResult<TaskCount> countStatistics(
      @Valid @ParameterObject TaskSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.countStatistics(dto));
  }

  @Operation(summary = "Export task count statistics",
      description = "Export comprehensive task count statistics in downloadable format with detailed filtering options",
      operationId = "analysis:task:count:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task statistics exported successfully")})
  @GetMapping(value = "/count/export")
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      @Valid @ParameterObject TaskSummaryStatisticsDto dto, HttpServletResponse response) {
    return analysisAngusTaskFacade.countStatisticsExport(dto, response);
  }

  @Operation(summary = "Assignee task count statistics",
      description = "Retrieve comprehensive task count statistics for individual assignees with performance metrics",
      operationId = "analysis:task:assignee:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Assignee task count statistics retrieved successfully")})
  @GetMapping("/assignee/count")
  public ApiLocaleResult<List<TaskAssigneeCount>> assigneeSummaryStatistics(
      @Valid @ParameterObject TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeSummaryStatistics(dto));
  }

  @Operation(summary = "Assignee task progress analysis",
      description = "Analyze task progress patterns for individual assignees with comprehensive progress metrics",
      operationId = "analysis:task:assignee:progress")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Assignee task progress analysis retrieved successfully")})
  @GetMapping("/assignee/progress")
  public ApiLocaleResult<List<TaskAssigneeProgressCount>> assigneeProgressStatistics(
      @Valid @ParameterObject TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeProgressStatistics(dto));
  }

  @Operation(summary = "Project task work summary",
      description = "Retrieve comprehensive work summary for tasks within a specific project",
      operationId = "analysis:task:project:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project task work summary retrieved successfully")})
  @GetMapping("/project/{projectId}/work/summary")
  public ApiLocaleResult<TaskProjectWorkSummary> projectWorkStatistics(
      @Parameter(name = "projectId", description = "Project identifier for work summary analysis", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectWorkStatistics(projectId));
  }

  @Operation(summary = "Sprint task work summary",
      description = "Retrieve comprehensive work summary for tasks within a specific sprint",
      operationId = "analysis:task:sprint:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint task work summary retrieved successfully")})
  @GetMapping("/sprint/{sprintId}/work/summary")
  public ApiLocaleResult<TaskSprintWorkSummary> sprintWorkStatistics(
      @Parameter(name = "sprintId", description = "Sprint identifier for work summary analysis", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintWorkStatistics(sprintId));
  }

  @Operation(summary = "Assignee task work summary",
      description = "Retrieve comprehensive work summary for tasks assigned to a specific assignee",
      operationId = "analysis:task:assignee:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Assignee task work summary retrieved successfully")})
  @GetMapping("/assignee/work/summary")
  public ApiLocaleResult<TaskAssigneeWorkSummary> assigneeWorkStatistics(
      @Valid @ParameterObject TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeWorkStatistics(dto));
  }

  @Operation(summary = "Project task burndown summary",
      description = "Retrieve comprehensive burndown chart data for tasks within a specific project",
      operationId = "analysis:task:project:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project task burndown summary retrieved successfully")})
  @GetMapping("/project/{projectId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> projectBurndownChart(
      @Parameter(name = "projectId", description = "Project identifier for burndown analysis", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectBurndownChart(projectId));
  }

  @Operation(summary = "Sprint task burndown summary",
      description = "Retrieve comprehensive burndown chart data for tasks within a specific sprint",
      operationId = "analysis:task:sprint:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sprint task burndown summary retrieved successfully")})
  @GetMapping("/sprint/{sprintId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> sprintBurndownChart(
      @Parameter(name = "sprintId", description = "Sprint identifier for burndown analysis", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintBurndownChart(sprintId));
  }

  @Operation(summary = "Assignee task burndown summary",
      description = "Retrieve comprehensive burndown chart data for tasks assigned to a specific assignee",
      operationId = "analysis:task:assignee:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Assignee task burndown summary retrieved successfully")})
  @GetMapping("/assignee/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> assigneeBurndownChart(
      @Valid @ParameterObject TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeBurndownChart(dto));
  }

  @Operation(summary = "Task progress overview",
      description = "Retrieve comprehensive progress overview for tasks with detailed progress metrics",
      operationId = "analysis:task:progress:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task progress overview retrieved successfully")})
  @GetMapping("/progress/overview")
  public ApiLocaleResult<ProgressOverview> progress(@Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.progress(dto));
  }

  @Operation(summary = "Task burndown overview",
      description = "Retrieve comprehensive burndown overview for tasks with detailed chart metrics",
      operationId = "analysis:task:burndown:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task burndown overview retrieved successfully")})
  @GetMapping("/burndown/overview")
  public ApiLocaleResult<BurnDownChartOverview> burndownChart(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.burndownChart(dto));
  }

  @Operation(summary = "Task workload overview",
      description = "Retrieve comprehensive workload overview for tasks with detailed workload metrics",
      operationId = "analysis:task:workload:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task workload overview retrieved successfully")})
  @GetMapping("/workload/overview")
  public ApiLocaleResult<WorkloadOverview> workload(@Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.workload(dto));
  }

  @Operation(summary = "Task overdue assessment overview",
      description = "Retrieve comprehensive overdue assessment overview for tasks with detailed overdue metrics",
      operationId = "analysis:task:overdue:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task overdue assessment overview retrieved successfully")})
  @GetMapping("/overdue/assessment/overview")
  public ApiLocaleResult<OverdueAssessmentOverview> overdue(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.overdueAssessment(dto));
  }

  @Operation(summary = "Task bug overview",
      description = "Retrieve comprehensive bug overview for tasks with detailed bug metrics and categorization",
      operationId = "analysis:task:bug:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task bug overview retrieved successfully")})
  @GetMapping("/bug/overview")
  public ApiLocaleResult<BugOverview> bug(@Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.bug(dto));
  }

  @Operation(summary = "Task processing efficiency overview",
      description = "Retrieve comprehensive processing efficiency overview for tasks with detailed efficiency metrics",
      operationId = "analysis:task:processing:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task processing efficiency overview retrieved successfully")})
  @GetMapping("/processing/efficiency/overview")
  public ApiLocaleResult<ProcessingEfficiencyOverview> processingEfficiency(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.processingEfficiency(dto));
  }

  @Operation(summary = "Task core KPI overview",
      description = "Retrieve comprehensive core KPI overview for tasks with detailed performance metrics",
      operationId = "analysis:task:core:kpi:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task core KPI overview retrieved successfully")})
  @GetMapping("/core/kpi/overview")
  public ApiLocaleResult<CoreKpiOverview> coreKpi(@Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.coreKpi(dto));
  }

  @Operation(summary = "Task failure assessment overview",
      description = "Retrieve comprehensive failure assessment overview for tasks with detailed failure metrics",
      operationId = "analysis:task:failure:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task failure assessment overview retrieved successfully")})
  @GetMapping("/failure/assessment/overview")
  public ApiLocaleResult<FailureAssessmentOverview> failureAssessment(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.failureAssessment(dto));
  }

  @Operation(summary = "Task backlog overview",
      description = "Retrieve comprehensive backlog overview for tasks with detailed backlog metrics",
      operationId = "analysis:task:backlogged:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task backlog overview retrieved successfully")})
  @GetMapping("/backlogged/overview")
  public ApiLocaleResult<BackloggedOverview> backloggedWork(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.backloggedWork(dto));
  }

  @Operation(summary = "Task recent delivery overview",
      description = "Retrieve comprehensive recent delivery overview for tasks with detailed delivery metrics",
      operationId = "analysis:task:recent:delivery:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task recent delivery overview retrieved successfully")})
  @GetMapping("/recent/delivery/overview")
  public ApiLocaleResult<RecentDeliveryOverview> recentDelivery(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.recentDelivery(dto));
  }

  @Operation(summary = "Task lead time overview",
      description = "Retrieve comprehensive lead time overview for tasks with detailed time metrics",
      operationId = "analysis:task:leadtime:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task lead time overview retrieved successfully")})
  @GetMapping("/leadtime/overview")
  public ApiLocaleResult<LeadTimeOverview> leadTime(@Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.leadTime(dto));
  }

  @Operation(summary = "Task unplanned work overview",
      description = "Retrieve comprehensive unplanned work overview for tasks with detailed unplanned work metrics",
      operationId = "analysis:task:unplanned:work:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task unplanned work overview retrieved successfully")})
  @GetMapping("/unplanned/work/overview")
  public ApiLocaleResult<UnplannedWorkOverview> unplannedWork(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.unplannedWork(dto));
  }

  @Operation(summary = "Task growth trend overview",
      description = "Retrieve comprehensive growth trend overview for tasks with detailed trend metrics",
      operationId = "analysis:task:growth:trend:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task growth trend overview retrieved successfully")})
  @GetMapping("/growth/trend/overview")
  public ApiLocaleResult<GrowthTrendOverview> growthTrend(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.growthTrend(dto));
  }

  @Operation(summary = "Task resource creation overview",
      description = "Retrieve comprehensive resource creation overview for tasks with detailed creation metrics",
      operationId = "analysis:task:resource:creation:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task resource creation overview retrieved successfully")})
  @GetMapping("/resource/creation/overview")
  public ApiLocaleResult<ResourceCreationOverview> resourceCreation(
      @Valid @ParameterObject TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.resourceCreation(dto));
  }

}
