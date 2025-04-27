package cloud.xcan.angus.core.tester.interfaces.analysis;


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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AnalysisTask", description = "Test Resource analytics - - Aggregated analytics for R&D tasks (sprint, backlog, task, bug, progress, etc).")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/task")
public class AnalysisTaskRest {

  @Resource
  private AnalysisTaskFacade analysisAngusTaskFacade;

  @Operation(description = "The count of tasks creation statistics. Task and associated module, plan and tag creation resources count statistics", operationId = "analysis:task:resources:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/resources/count")
  public ApiLocaleResult<TaskLastResourceCreationCount> creationResourcesStatistics(
      @Valid TaskCreatorStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.creationResourcesStatistics(dto));
  }

  @Operation(description = "The count of task statistics", operationId = "analysis:task:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/count")
  public ApiLocaleResult<TaskCount> countStatistics(@Valid TaskSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.countStatistics(dto));
  }

  @Operation(description = "Export the count of task statistics", operationId = "analysis:task:count:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Exported Successfully")})
  @GetMapping(value = "/count/export")
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      @Valid TaskSummaryStatisticsDto dto, HttpServletResponse response) {
    return analysisAngusTaskFacade.countStatisticsExport(dto, response);
  }

  @Operation(description = "The count of tasks statistics for assignee", operationId = "analysis:task:assignee:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/assignee/count")
  public ApiLocaleResult<List<TaskAssigneeCount>> assigneeSummaryStatistics(
      @Valid TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeSummaryStatistics(dto));
  }

  @Operation(description = "The progress of tasks for assignee", operationId = "analysis:task:assignee:progress")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/assignee/progress")
  public ApiLocaleResult<List<TaskAssigneeProgressCount>> assigneeProgressStatistics(
      @Valid TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeProgressStatistics(dto));
  }

  @Operation(description = "The work summary of tasks for project", operationId = "analysis:task:project:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/project/{projectId}/work/summary")
  public ApiLocaleResult<TaskProjectWorkSummary> projectWorkStatistics(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectWorkStatistics(projectId));
  }

  @Operation(description = "The work summary of tasks for sprint", operationId = "analysis:task:sprint:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/sprint/{sprintId}/work/summary")
  public ApiLocaleResult<TaskSprintWorkSummary> sprintWorkStatistics(
      @Parameter(name = "sprintId", description = "Sprint id", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintWorkStatistics(sprintId));
  }

  @Operation(description = "The work summary of tasks for assignee", operationId = "analysis:task:assignee:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/assignee/work/summary")
  public ApiLocaleResult<TaskAssigneeWorkSummary> assigneeWorkStatistics(
      @Valid TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeWorkStatistics(dto));
  }

  @Operation(description = "The burndown summary of tasks for project", operationId = "analysis:task:project:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/project/{projectId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> projectBurndownChart(
      @Parameter(name = "projectId", description = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectBurndownChart(projectId));
  }

  @Operation(description = "The burndown summary of tasks for sprint", operationId = "analysis:task:sprint:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/sprint/{sprintId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> sprintBurndownChart(
      @Parameter(name = "sprintId", description = "Sprint id", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintBurndownChart(sprintId));
  }

  @Operation(description = "The burndown summary of tasks for assignee", operationId = "analysis:task:assignee:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/assignee/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> assigneeBurndownChart(
      @Valid TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeBurndownChart(dto));
  }

  @Operation(description = "The overview of tasks progress", operationId = "analysis:task:progress:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/progress/overview")
  public ApiLocaleResult<ProgressOverview> progress(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.progress(dto));
  }

  @Operation(description = "The overview of tasks burndown", operationId = "analysis:task:burndown:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/burndown/overview")
  public ApiLocaleResult<BurnDownChartOverview> burndownChart(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.burndownChart(dto));
  }

  @Operation(description = "The overview of tasks workload", operationId = "analysis:task:workload:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/workload/overview")
  public ApiLocaleResult<WorkloadOverview> workload(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.workload(dto));
  }

  @Operation(description = "The overview of overdue tasks assessment", operationId = "analysis:task:overdue:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/overdue/assessment/overview")
  public ApiLocaleResult<OverdueAssessmentOverview> overdue(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.overdueAssessment(dto));
  }

  @Operation(description = "The overview of bug type tasks", operationId = "analysis:task:bug:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/bug/overview")
  public ApiLocaleResult<BugOverview> bug(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.bug(dto));
  }

  @Operation(description = "The overview of tasks processing efficiency", operationId = "analysis:task:processing:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/processing/efficiency/overview")
  public ApiLocaleResult<ProcessingEfficiencyOverview> processingEfficiency(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.processingEfficiency(dto));
  }

  @Operation(description = "The overview of tasks core KPI", operationId = "analysis:task:core:kpi:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/core/kpi/overview")
  public ApiLocaleResult<CoreKpiOverview> coreKpi(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.coreKpi(dto));
  }

  @Operation(description = "The overview of tasks failure assessment", operationId = "analysis:task:failure:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/failure/assessment/overview")
  public ApiLocaleResult<FailureAssessmentOverview> failureAssessment(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.failureAssessment(dto));
  }

  @Operation(description = "The overview of backlogged tasks", operationId = "analysis:task:backlogged:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/backlogged/overview")
  public ApiLocaleResult<BackloggedOverview> backloggedWork(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.backloggedWork(dto));
  }

  @Operation(description = "The overview of recent delivery tasks", operationId = "analysis:task:recent:delivery:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/recent/delivery/overview")
  public ApiLocaleResult<RecentDeliveryOverview> recentDelivery(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.recentDelivery(dto));
  }

  @Operation(description = "The overview of tasks lead time", operationId = "analysis:task:leadtime:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/leadtime/overview")
  public ApiLocaleResult<LeadTimeOverview> leadTime(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.leadTime(dto));
  }

  @Operation(description = "The overview of unplanned work tasks", operationId = "analysis:task:unplanned:work:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/unplanned/work/overview")
  public ApiLocaleResult<UnplannedWorkOverview> unplannedWork(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.unplannedWork(dto));
  }

  @Operation(description = "The overview of tasks growth trend", operationId = "analysis:task:growth:trend:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/growth/trend/overview")
  public ApiLocaleResult<GrowthTrendOverview> growthTrend(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.growthTrend(dto));
  }

  @Operation(description = "The overview of tasks resource creation", operationId = "analysis:task:resource:creation:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/resource/creation/overview")
  public ApiLocaleResult<ResourceCreationOverview> resourceCreation(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.resourceCreation(dto));
  }

}
