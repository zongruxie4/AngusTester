package cloud.xcan.angus.core.tester.interfaces.analysis;


import cloud.xcan.angus.core.tester.domain.func.cases.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterProgressCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ReviewEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.TestingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncPlanWorkSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncTesterWorkSummary;
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

@Tag(name = "Analysis Functional Testing", description = "Functional Testing Analytics - Comprehensive analytics for functional testing resources including test plans, cases, progress tracking, efficiency metrics, and performance insights")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/func")
public class AnalysisFuncRest {

  @Resource
  private AnalysisFuncFacade analysisAngusFuncFacade;

  @Operation(summary = "Functional case creation statistics", 
      description = "Retrieve comprehensive statistics on functional case creation patterns including associated plans, tags, and modules",
      operationId = "analysis:func:resources:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case creation statistics retrieved successfully")})
  @GetMapping("/resources/count")
  public ApiLocaleResult<FuncLastResourceCreationCount> funcResourcesStatistics(
      @Valid @ParameterObject FuncCreatorStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.creationResourcesStatistics(dto));
  }

  @Operation(summary = "Functional case count statistics", 
      description = "Query and analyze functional case counts with comprehensive filtering and categorization metrics",
      operationId = "analysis:func:case:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case count statistics retrieved successfully")})
  @GetMapping("/case/count")
  public ApiLocaleResult<FuncCaseCount> countStatistics(
      @Valid @ParameterObject FuncCaseSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.countStatistics(dto));
  }

  @Operation(summary = "Export functional case count statistics", 
      description = "Export comprehensive functional case count statistics in downloadable format with detailed filtering options",
      operationId = "analysis:func:case:count:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional case statistics exported successfully")})
  @GetMapping(value = "/case/count/export")
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      @Valid @ParameterObject FuncCaseSummaryStatisticsDto dto, HttpServletResponse response) {
    return analysisAngusFuncFacade.countStatisticsExport(dto, response);
  }

  @Operation(summary = "Tester case count statistics", 
      description = "Retrieve comprehensive case count statistics for individual testers with performance metrics",
      operationId = "analysis:func:tester:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tester case count statistics retrieved successfully")})
  @GetMapping("/tester/count")
  public ApiLocaleResult<List<FuncTesterCount>> testerSummaryStatistics(
      @Valid @ParameterObject FuncTesterSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerSummaryStatistics(dto));
  }

  @Operation(summary = "Tester case progress analysis", 
      description = "Analyze case progress patterns for individual testers with comprehensive progress metrics",
      operationId = "analysis:func:tester:progress")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tester case progress analysis retrieved successfully")})
  @GetMapping("/tester/progress")
  public ApiLocaleResult<List<FuncTesterProgressCount>> testerProgressStatistics(
      @Valid @ParameterObject FuncTesterSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerProgressStatistics(dto));
  }

  @Operation(summary = "Project case work summary", 
      description = "Retrieve comprehensive work summary for functional cases within a specific project",
      operationId = "analysis:func:project:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project case work summary retrieved successfully")})
  @GetMapping("/project/{projectId}/work/summary")
  public ApiLocaleResult<FuncProjectWorkSummary> projectWorkStatistics(
      @Parameter(name = "projectId", description = "Project identifier for work summary analysis", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.projectWorkStatistics(projectId));
  }

  @Operation(summary = "Test plan case work summary", 
      description = "Retrieve comprehensive work summary for functional cases within a specific test plan",
      operationId = "analysis:func:plan:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan case work summary retrieved successfully")})
  @GetMapping("/plan/{planId}/work/summary")
  public ApiLocaleResult<FuncPlanWorkSummary> planWorkStatistics(
      @Parameter(name = "planId", description = "Test plan identifier for work summary analysis", required = true) @PathVariable("planId") Long planId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.planWorkStatistics(planId));
  }

  @Operation(summary = "Tester case work summary", 
      description = "Retrieve comprehensive work summary for functional cases assigned to a specific tester",
      operationId = "analysis:func:tester:work:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tester case work summary retrieved successfully")})
  @GetMapping("/tester/work/summary")
  public ApiLocaleResult<FuncTesterWorkSummary> testerWorkStatistics(
      @Valid @ParameterObject FuncTesterWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerWorkStatistics(dto));
  }

  @Operation(summary = "Project case burndown summary", 
      description = "Retrieve comprehensive burndown chart data for functional cases within a specific project",
      operationId = "analysis:func:project:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project case burndown summary retrieved successfully")})
  @GetMapping("/project/{projectId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> projectBurndownChart(
      @Parameter(name = "projectId", description = "Project identifier for burndown analysis", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.projectBurndownChart(projectId));
  }

  @Operation(summary = "Test plan case burndown summary", 
      description = "Retrieve comprehensive burndown chart data for functional cases within a specific test plan",
      operationId = "analysis:func:plan:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan case burndown summary retrieved successfully")})
  @GetMapping("/plan/{planId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> planBurndownChart(
      @Parameter(name = "planId", description = "Test plan identifier for burndown analysis", required = true) @PathVariable("planId") Long planId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.planBurndownChart(planId));
  }

  @Operation(summary = "Tester case burndown summary", 
      description = "Retrieve comprehensive burndown chart data for functional cases assigned to a specific tester",
      operationId = "analysis:func:tester:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tester case burndown summary retrieved successfully")})
  @GetMapping("/tester/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> testerBurndownChart(
      @Valid @ParameterObject CaseTesterWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerBurndownChart(dto));
  }

  @Operation(summary = "Functional case progress overview", 
      description = "Retrieve comprehensive progress overview for functional cases with detailed progress metrics",
      operationId = "analysis:func:progress:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case progress overview retrieved successfully")})
  @GetMapping("/progress/overview")
  public ApiLocaleResult<ProgressOverview> progress(@Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.progress(dto));
  }

  @Operation(summary = "Functional case burndown overview", 
      description = "Retrieve comprehensive burndown overview for functional cases with detailed chart metrics",
      operationId = "analysis:func:burndown:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case burndown overview retrieved successfully")})
  @GetMapping("/burndown/overview")
  public ApiLocaleResult<BurnDownChartOverview> burndownChart(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.burndownChart(dto));
  }

  @Operation(summary = "Functional case workload overview", 
      description = "Retrieve comprehensive workload overview for functional cases with detailed workload metrics",
      operationId = "analysis:func:workload:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case workload overview retrieved successfully")})
  @GetMapping("/workload/overview")
  public ApiLocaleResult<WorkloadOverview> workload(@Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.workload(dto));
  }

  @Operation(summary = "Functional case overdue assessment overview", 
      description = "Retrieve comprehensive overdue assessment overview for functional cases with detailed overdue metrics",
      operationId = "analysis:func:overdue:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case overdue assessment overview retrieved successfully")})
  @GetMapping("/overdue/assessment/overview")
  public ApiLocaleResult<OverdueAssessmentOverview> overdueAssessment(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.overdueAssessment(dto));
  }

  @Operation(summary = "Tester submitted bug overview", 
      description = "Retrieve comprehensive overview of bugs submitted by testers with detailed bug metrics",
      operationId = "analysis:func:submitted:bug:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tester submitted bug overview retrieved successfully")})
  @GetMapping("/submitted/bug/overview")
  public ApiLocaleResult<TesterSubmittedBugOverview> submittedBug(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.submittedBug(dto));
  }

  @Operation(summary = "Functional case testing efficiency overview", 
      description = "Retrieve comprehensive testing efficiency overview for functional cases with detailed efficiency metrics",
      operationId = "analysis:func:testing:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case testing efficiency overview retrieved successfully")})
  @GetMapping("/testing/efficiency/overview")
  public ApiLocaleResult<TestingEfficiencyOverview> testingEfficiency(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testingEfficiency(dto));
  }

  @Operation(summary = "Functional case core KPI overview", 
      description = "Retrieve comprehensive core KPI overview for functional cases with detailed performance metrics",
      operationId = "analysis:func:core:kpi:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case core KPI overview retrieved successfully")})
  @GetMapping("/core/kpi/overview")
  public ApiLocaleResult<CoreKpiOverview> coreKpi(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.coreKpi(dto));
  }

  @Operation(summary = "Functional case review efficiency overview", 
      description = "Retrieve comprehensive review efficiency overview for functional cases with detailed review metrics",
      operationId = "analysis:func:review:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case review efficiency overview retrieved successfully")})
  @GetMapping("/review/efficiency/overview")
  public ApiLocaleResult<ReviewEfficiencyOverview> reviewEfficiency(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.reviewEfficiency(dto));
  }

  @Operation(summary = "Functional case backlog overview", 
      description = "Retrieve comprehensive backlog overview for functional cases with detailed backlog metrics",
      operationId = "analysis:func:backlogged:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case backlog overview retrieved successfully")})
  @GetMapping("/backlogged/overview")
  public ApiLocaleResult<BackloggedOverview> backloggedWork(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.backloggedWork(dto));
  }

  @Operation(summary = "Functional case recent delivery overview", 
      description = "Retrieve comprehensive recent delivery overview for functional cases with detailed delivery metrics",
      operationId = "analysis:func:recent:delivery:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case recent delivery overview retrieved successfully")})
  @GetMapping("/recent/delivery/overview")
  public ApiLocaleResult<RecentDeliveryOverview> recentDelivery(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.recentDelivery(dto));
  }

  @Operation(summary = "Functional case lead time overview", 
      description = "Retrieve comprehensive lead time overview for functional cases with detailed time metrics",
      operationId = "analysis:func:leadtime:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case lead time overview retrieved successfully")})
  @GetMapping("/leadtime/overview")
  public ApiLocaleResult<LeadTimeOverview> leadTime(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.leadTime(dto));
  }

  @Operation(summary = "Functional case unplanned work overview", 
      description = "Retrieve comprehensive unplanned work overview for functional cases with detailed unplanned work metrics",
      operationId = "analysis:func:unplanned:work:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case unplanned work overview retrieved successfully")})
  @GetMapping("/unplanned/work/overview")
  public ApiLocaleResult<UnplannedWorkOverview> unplannedWork(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.unplannedWork(dto));
  }

  @Operation(summary = "Functional case growth trend overview", 
      description = "Retrieve comprehensive growth trend overview for functional cases with detailed trend metrics",
      operationId = "analysis:func:growth:trend:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case growth trend overview retrieved successfully")})
  @GetMapping("/growth/trend/overview")
  public ApiLocaleResult<GrowthTrendOverview> growthTrend(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.growthTrend(dto));
  }

  @Operation(summary = "Functional case resource creation overview", 
      description = "Retrieve comprehensive resource creation overview for functional cases with detailed creation metrics",
      operationId = "analysis:func:resource:creation:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case resource creation overview retrieved successfully")})
  @GetMapping("/resource/creation/overview")
  public ApiLocaleResult<ResourceCreationOverview> resourceCreation(
      @Valid @ParameterObject FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.resourceCreation(dto));
  }

}
