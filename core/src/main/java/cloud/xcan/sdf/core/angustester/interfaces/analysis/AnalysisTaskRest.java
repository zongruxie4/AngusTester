package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BugOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.CoreKpiOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.GrowthTrendOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProcessingEfficiencyOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.ResourceCreationOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadOverview;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskAssigneeWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskProjectWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSprintWorkSummary;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisTaskFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskAnalysisDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskAssigneeSummaryStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskAssigneeWorkStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskCreatorStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskSummaryStatisticsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "AnalysisTask")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/task")
public class AnalysisTaskRest {

  @Resource
  private AnalysisTaskFacade analysisAngusTaskFacade;

  @ApiOperation(value = "The count of tasks creation statistics. Task and associated module, plan and tag creation resources count statistics", nickname = "analysis:task:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/resources/count")
  public ApiLocaleResult<TaskLastResourceCreationCount> creationResourcesStatistics(
      @Valid TaskCreatorStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.creationResourcesStatistics(dto));
  }

  @ApiOperation(value = "The count of task statistics", nickname = "analysis:task:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/count")
  public ApiLocaleResult<TaskCount> countStatistics(@Valid TaskSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.countStatistics(dto));
  }

  @ApiOperation(value = "Export the count of task statistics", nickname = "analysis:task:count:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/count/export")
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      @Valid TaskSummaryStatisticsDto dto, HttpServletResponse response) {
    return analysisAngusTaskFacade.countStatisticsExport(dto, response);
  }

  @ApiOperation(value = "The count of tasks statistics for assignee", nickname = "analysis:task:assignee:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/assignee/count")
  public ApiLocaleResult<List<TaskAssigneeCount>> assigneeSummaryStatistics(
      @Valid TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeSummaryStatistics(dto));
  }

  @ApiOperation(value = "The progress of tasks for assignee", nickname = "analysis:task:assignee:progress")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/assignee/progress")
  public ApiLocaleResult<List<TaskAssigneeProgressCount>> assigneeProgressStatistics(
      @Valid TaskAssigneeSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeProgressStatistics(dto));
  }

  @ApiOperation(value = "The work summary of tasks for project", nickname = "analysis:task:project:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/project/{projectId}/work/summary")
  public ApiLocaleResult<TaskProjectWorkSummary> projectWorkStatistics(
      @ApiParam(name = "projectId", value = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectWorkStatistics(projectId));
  }

  @ApiOperation(value = "The work summary of tasks for sprint", nickname = "analysis:task:sprint:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/sprint/{sprintId}/work/summary")
  public ApiLocaleResult<TaskSprintWorkSummary> sprintWorkStatistics(
      @ApiParam(name = "sprintId", value = "Sprint id", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintWorkStatistics(sprintId));
  }

  @ApiOperation(value = "The work summary of tasks for assignee", nickname = "analysis:task:assignee:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/assignee/work/summary")
  public ApiLocaleResult<TaskAssigneeWorkSummary> assigneeWorkStatistics(
      @Valid TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeWorkStatistics(dto));
  }

  @ApiOperation(value = "The burndown summary of tasks for project", nickname = "analysis:task:project:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/project/{projectId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> projectBurndownChart(
      @ApiParam(name = "projectId", value = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.projectBurndownChart(projectId));
  }

  @ApiOperation(value = "The burndown summary of tasks for sprint", nickname = "analysis:task:sprint:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/sprint/{sprintId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> sprintBurndownChart(
      @ApiParam(name = "sprintId", value = "Sprint id", required = true) @PathVariable("sprintId") Long sprintId) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.sprintBurndownChart(sprintId));
  }

  @ApiOperation(value = "The burndown summary of tasks for assignee", nickname = "analysis:task:assignee:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/assignee/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> assigneeBurndownChart(
      @Valid TaskAssigneeWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.assigneeBurndownChart(dto));
  }

  @ApiOperation(value = "The overview of tasks progress", nickname = "analysis:task:progress:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/progress/overview")
  public ApiLocaleResult<ProgressOverview> progress(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.progress(dto));
  }

  @ApiOperation(value = "The overview of tasks burndown", nickname = "analysis:task:burndown:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/burndown/overview")
  public ApiLocaleResult<BurnDownChartOverview> burndownChart(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.burndownChart(dto));
  }

  @ApiOperation(value = "The overview of tasks workload", nickname = "analysis:task:workload:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/workload/overview")
  public ApiLocaleResult<WorkloadOverview> workload(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.workload(dto));
  }

  @ApiOperation(value = "The overview of overdue tasks assessment", nickname = "analysis:task:overdue:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/overdue/assessment/overview")
  public ApiLocaleResult<OverdueAssessmentOverview> overdue(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.overdueAssessment(dto));
  }

  @ApiOperation(value = "The overview of bug type tasks", nickname = "analysis:task:bug:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/bug/overview")
  public ApiLocaleResult<BugOverview> bug(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.bug(dto));
  }

  @ApiOperation(value = "The overview of tasks processing efficiency", nickname = "analysis:task:processing:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/processing/efficiency/overview")
  public ApiLocaleResult<ProcessingEfficiencyOverview> processingEfficiency(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.processingEfficiency(dto));
  }

  @ApiOperation(value = "The overview of tasks core KPI", nickname = "analysis:task:core:kpi:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/core/kpi/overview")
  public ApiLocaleResult<CoreKpiOverview> coreKpi(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.coreKpi(dto));
  }

  @ApiOperation(value = "The overview of tasks failure assessment", nickname = "analysis:task:failure:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/failure/assessment/overview")
  public ApiLocaleResult<FailureAssessmentOverview> failureAssessment(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.failureAssessment(dto));
  }

  @ApiOperation(value = "The overview of backlogged tasks", nickname = "analysis:task:backlogged:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/backlogged/overview")
  public ApiLocaleResult<BackloggedOverview> backloggedWork(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.backloggedWork(dto));
  }

  @ApiOperation(value = "The overview of recent delivery tasks", nickname = "analysis:task:recent:delivery:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/recent/delivery/overview")
  public ApiLocaleResult<RecentDeliveryOverview> recentDelivery(
      @Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.recentDelivery(dto));
  }

  @ApiOperation(value = "The overview of tasks lead time", nickname = "analysis:task:leadtime:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/leadtime/overview")
  public ApiLocaleResult<LeadTimeOverview> leadTime(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.leadTime(dto));
  }

  @ApiOperation(value = "The overview of unplanned work tasks", nickname = "analysis:task:unplanned:work:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/unplanned/work/overview")
  public ApiLocaleResult<UnplannedWorkOverview> unplannedWork(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.unplannedWork(dto));
  }

  @ApiOperation(value = "The overview of tasks growth trend", nickname = "analysis:task:growth:trend:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/growth/trend/overview")
  public ApiLocaleResult<GrowthTrendOverview> growthTrend(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.growthTrend(dto));
  }

  @ApiOperation(value = "The overview of tasks resource creation", nickname = "analysis:task:resource:creation:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/resource/creation/overview")
  public ApiLocaleResult<ResourceCreationOverview> resourceCreation(@Valid TaskAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusTaskFacade.resourceCreation(dto));
  }

}
