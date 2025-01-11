package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import cloud.xcan.sdf.api.ApiLocaleResult;
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
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncPlanWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncProjectWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncTesterWorkSummary;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TesterSubmittedBugOverview;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisFuncFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.CaseTesterWorkStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncAnalysisDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncCaseSummaryStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncCreatorStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncTesterSummaryStatisticsDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncTesterWorkStatisticsDto;
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

@Api(tags = "AnalysisFunc")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/func")
public class AnalysisFuncRest {

  @Resource
  private AnalysisFuncFacade analysisAngusFuncFacade;

  @ApiOperation(value = "The count of cases creation statistics. Cases and associated plan, tag, and module creation count statistics", nickname = "analysis:func:resources:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/resources/count")
  public ApiLocaleResult<FuncLastResourceCreationCount> funcResourcesStatistics(
      @Valid FuncCreatorStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.creationResourcesStatistics(dto));
  }

  @ApiOperation(value = "The count of cases statistics", nickname = "analysis:func:case:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/case/count")
  public ApiLocaleResult<FuncCaseCount> countStatistics(@Valid FuncCaseSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.countStatistics(dto));
  }

  @ApiOperation(value = "Export the count of cases statistics", nickname = "analysis:func:case:count:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/case/count/export")
  public ResponseEntity<org.springframework.core.io.Resource> countStatisticsExport(
      @Valid FuncCaseSummaryStatisticsDto dto, HttpServletResponse response) {
    return analysisAngusFuncFacade.countStatisticsExport(dto, response);
  }

  @ApiOperation(value = "The count of case statistics for tester", nickname = "analysis:func:tester:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tester/count")
  public ApiLocaleResult<List<FuncTesterCount>> testerSummaryStatistics(
      @Valid FuncTesterSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerSummaryStatistics(dto));
  }

  @ApiOperation(value = "The case progress analysis of testers", nickname = "analysis:func:tester:progress")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tester/progress")
  public ApiLocaleResult<List<FuncTesterProgressCount>> testerProgressStatistics(
      @Valid FuncTesterSummaryStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerProgressStatistics(dto));
  }

  @ApiOperation(value = "The work summary of cases for project", nickname = "analysis:func:project:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/project/{projectId}/work/summary")
  public ApiLocaleResult<FuncProjectWorkSummary> projectWorkStatistics(
      @ApiParam(name = "projectId", value = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.projectWorkStatistics(projectId));
  }

  @ApiOperation(value = "The work summary of cases for plan", nickname = "analysis:func:plan:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/plan/{planId}/work/summary")
  public ApiLocaleResult<FuncPlanWorkSummary> planWorkStatistics(
      @ApiParam(name = "planId", value = "Plan id", required = true) @PathVariable("planId") Long planId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.planWorkStatistics(planId));
  }

  @ApiOperation(value = "The work summary of cases for tester", nickname = "analysis:func:tester:work:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tester/work/summary")
  public ApiLocaleResult<FuncTesterWorkSummary> testerWorkStatistics(
      @Valid FuncTesterWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerWorkStatistics(dto));
  }

  @ApiOperation(value = "The summary of cases burndown for project", nickname = "analysis:func:project:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/project/{projectId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> projectBurndownChart(
      @ApiParam(name = "projectId", value = "Project id", required = true) @PathVariable("projectId") Long projectId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.projectBurndownChart(projectId));
  }

  @ApiOperation(value = "The summary of cases burndown for plan", nickname = "analysis:func:plan:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/plan/{planId}/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> planBurndownChart(
      @ApiParam(name = "planId", value = "Plan id", required = true) @PathVariable("planId") Long planId) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.planBurndownChart(planId));
  }

  @ApiOperation(value = "The summary of cases burndown for tester", nickname = "analysis:func:tester:burndown:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tester/burndown")
  public ApiLocaleResult<Map<BurnDownResourceType, BurnDownChartCount>> testerBurndownChart(
      @Valid CaseTesterWorkStatisticsDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testerBurndownChart(dto));
  }

  @ApiOperation(value = "The overview of cases progress", nickname = "analysis:func:progress:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/progress/overview")
  public ApiLocaleResult<ProgressOverview> progress(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.progress(dto));
  }

  @ApiOperation(value = "The overview of cases burndown", nickname = "analysis:func:burndown:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/burndown/overview")
  public ApiLocaleResult<BurnDownChartOverview> burndownChart(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.burndownChart(dto));
  }

  @ApiOperation(value = "The overview of cases workload", nickname = "analysis:func:workload:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/workload/overview")
  public ApiLocaleResult<WorkloadOverview> workload(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.workload(dto));
  }

  @ApiOperation(value = "The overview of overdue cases assessment", nickname = "analysis:func:overdue:assessment:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/overdue/assessment/overview")
  public ApiLocaleResult<OverdueAssessmentOverview> overdueAssessment(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.overdueAssessment(dto));
  }

  @ApiOperation(value = "The overview of tester submitted bug", nickname = "analysis:func:submitted:bug:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/submitted/bug/overview")
  public ApiLocaleResult<TesterSubmittedBugOverview> submittedBug(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.submittedBug(dto));
  }

  @ApiOperation(value = "The overview of cases testing efficiency", nickname = "analysis:func:testing:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/testing/efficiency/overview")
  public ApiLocaleResult<TestingEfficiencyOverview> testingEfficiency(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.testingEfficiency(dto));
  }

  @ApiOperation(value = "The overview of cases core KPI", nickname = "analysis:func:core:kpi:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/core/kpi/overview")
  public ApiLocaleResult<CoreKpiOverview> coreKpi(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.coreKpi(dto));
  }

  @ApiOperation(value = "The overview of cases review efficiency", nickname = "analysis:func:review:efficiency:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/review/efficiency/overview")
  public ApiLocaleResult<ReviewEfficiencyOverview> reviewEfficiency(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.reviewEfficiency(dto));
  }

  @ApiOperation(value = "The overview of backlogged cases", nickname = "analysis:func:backlogged:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/backlogged/overview")
  public ApiLocaleResult<BackloggedOverview> backloggedWork(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.backloggedWork(dto));
  }

  @ApiOperation(value = "The overview of recent delivery cases", nickname = "analysis:func:recent:delivery:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/recent/delivery/overview")
  public ApiLocaleResult<RecentDeliveryOverview> recentDelivery(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.recentDelivery(dto));
  }

  @ApiOperation(value = "The overview of cases lead time", nickname = "analysis:func:leadtime:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/leadtime/overview")
  public ApiLocaleResult<LeadTimeOverview> leadTime(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.leadTime(dto));
  }

  @ApiOperation(value = "The overview of unplanned work cases", nickname = "analysis:func:unplanned:work:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/unplanned/work/overview")
  public ApiLocaleResult<UnplannedWorkOverview> unplannedWork(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.unplannedWork(dto));
  }

  @ApiOperation(value = "The overview of case growth trend", nickname = "analysis:func:growth:trend:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/growth/trend/overview")
  public ApiLocaleResult<GrowthTrendOverview> growthTrend(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.growthTrend(dto));
  }

  @ApiOperation(value = "The overview of function resource creation", nickname = "analysis:func:resource:creation:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/resource/creation/overview")
  public ApiLocaleResult<ResourceCreationOverview> resourceCreation(@Valid FuncAnalysisDto dto) {
    return ApiLocaleResult.success(analysisAngusFuncFacade.resourceCreation(dto));
  }

}
