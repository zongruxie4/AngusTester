package cloud.xcan.angus.core.tester.interfaces.report;


import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportRecordFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ApisTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncPlanContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ProjectProgressContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ReportContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ScenarioTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ServicesTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskSprintContentVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Conditional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report Content", description = "Report Content Query - "
    + "Provides a centralized entry point for querying all types of report content within the system, "
    + "supporting various report formats and content types")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotCommunityEditionCondition.class)
public class ReportContentRest {

  @Resource
  private ReportRecordFacade reportRecordFacade;

  @Operation(summary = "Query comprehensive report content", description = "Retrieve complete report content including all sections and data, optionally for a specific record version", operationId = "report:content:wide:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/wide/content")
  public ApiLocaleResult<ReportContentVo> reportContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.reportContent(reportId, recordId));
  }

  @Operation(summary = "Query project progress content", description = "Retrieve project progress report content with detailed progress information and milestones", operationId = "report:projectProgress:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project progress content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/projectProgress/content")
  public ApiLocaleResult<ProjectProgressContentVo> projectProgressContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.projectProgressContent(reportId, recordId));
  }

  @Operation(summary = "Query task sprint content", description = "Retrieve task sprint report content with sprint details and task breakdowns", operationId = "report:taskSprint:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task sprint content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/taskSprint/content")
  public ApiLocaleResult<TaskSprintContentVo> taskSprintContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.taskSprintContent(reportId, recordId));
  }

  @Operation(summary = "Query task content", description = "Retrieve task report content with detailed task information and status", operationId = "report:task:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/task/content")
  public ApiLocaleResult<TaskContentVo> taskContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.taskContent(reportId, recordId));
  }

  @Operation(summary = "Query functional plan content", description = "Retrieve functional plan report content with testing plans and requirements", operationId = "report:funcPlan:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional plan content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/funcPlan/content")
  public ApiLocaleResult<FuncPlanContentVo> funcPlanContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.funcPlanContent(reportId, recordId));
  }

  @Operation(summary = "Query functional case content", description = "Retrieve functional case report content with test case details and execution results", operationId = "report:funcCase:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/funcCase/content")
  public ApiLocaleResult<FuncCaseContentVo> funcCaseContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.funcCaseContent(reportId, recordId));
  }

  @Operation(summary = "Query services testing content", description = "Retrieve services testing report content with service test results and performance metrics", operationId = "report:servicesTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Services testing content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/servicesTesting/content")
  public ApiLocaleResult<ServicesTestingContentVo> servicesTestingContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.servicesTestingContent(reportId, recordId));
  }

  @Operation(summary = "Query APIs testing content", description = "Retrieve APIs testing report content with API test results and endpoint coverage", operationId = "report:apisTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs testing content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/apisTesting/content")
  public ApiLocaleResult<ApisTestingContentVo> apisTestingContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.apisTestingContent(reportId, recordId));
  }

  @Operation(summary = "Query scenario testing content", description = "Retrieve scenario testing report content with end-to-end test scenarios and results", operationId = "report:scenarioTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario testing content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report or record not found")})
  @GetMapping(value = "/{reportId}/scenarioTesting/content")
  public ApiLocaleResult<ScenarioTestingContentVo> scenarioTestingContent(
      @Parameter(name = "reportId", description = "Report ID", required = true) @PathVariable("reportId") Long reportId,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.scenarioTestingContent(reportId, recordId));
  }

}
