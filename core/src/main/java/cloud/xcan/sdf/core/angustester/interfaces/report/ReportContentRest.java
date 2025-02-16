package cloud.xcan.sdf.core.angustester.interfaces.report;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportRecordFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ApisTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncPlanContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ProjectProgressContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ReportContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ScenarioTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ServicesTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskSprintContentVo;
import cloud.xcan.sdf.core.spring.condition.NotCommunityEditionCondition;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.context.annotation.Conditional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ReportContent")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotCommunityEditionCondition.class)
public class ReportContentRest {

  @Resource
  private ReportRecordFacade reportRecordFacade;

  @ApiOperation(value = "Query the content of report or record", nickname = "report:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/wide/content")
  public ApiLocaleResult<ReportContentVo> reportContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.reportContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of project progress report or record", nickname = "report:projectProgress:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/projectProgress/content")
  public ApiLocaleResult<ProjectProgressContentVo> projectProgressContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.projectProgressContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of task sprint report or record", nickname = "report:taskSprint:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/taskSprint/content")
  public ApiLocaleResult<TaskSprintContentVo> taskSprintContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.taskSprintContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of task report or record", nickname = "report:task:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/task/content")
  public ApiLocaleResult<TaskContentVo> taskContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.taskContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of testing report report or record", nickname = "report:funcPlan:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/funcPlan/content")
  public ApiLocaleResult<FuncPlanContentVo> funcPlanContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.funcPlanContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of testing case report or record", nickname = "report:funcCase:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/funcCase/content")
  public ApiLocaleResult<FuncCaseContentVo> funcCaseContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.funcCaseContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of services testing report or record", nickname = "report:servicesTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/servicesTesting/content")
  public ApiLocaleResult<ServicesTestingContentVo> servicesTestingContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.servicesTestingContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of apis testing report or record", nickname = "report:apisTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/apisTesting/content")
  public ApiLocaleResult<ApisTestingContentVo> apisTestingContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.apisTestingContent(reportId, recordId));
  }

  @ApiOperation(value = "Query the content of scenario testing report or record", nickname = "report:apisTesting:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{reportId}/scenarioTesting/content")
  public ApiLocaleResult<ScenarioTestingContentVo> scenarioTestingContent(
      @ApiParam(name = "reportId", value = "Report id", required = true) @PathVariable("reportId") Long reportId,
      @ApiParam(name = "recordId", value = "Report record id", required = false) @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.success(reportRecordFacade.scenarioTestingContent(reportId, recordId));
  }

}
