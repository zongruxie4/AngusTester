package cloud.xcan.angus.core.tester.interfaces.report;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "Report Configuration and Management - "
    + "Provides a unified entry point for configuring and managing report settings, "
    + "facilitating efficient customization and oversight of reporting processes")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotCommunityEditionCondition.class)
public class ReportRest {

  @Resource
  private ReportFacade reportFacade;

  @Operation(summary = "Create report", description = "Create a new report configuration with specified settings and parameters", operationId = "report:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Report created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ReportAddDto dto) {
    return ApiLocaleResult.success(reportFacade.add(dto));
  }

  @Operation(summary = "Update report", description = "Update configuration settings of an existing report", operationId = "report:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report updated successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ReportUpdateDto dto) {
    reportFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace report", description = "Completely replace all configuration information of an existing report", operationId = "report:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ReportReplaceDto dto) {
    return ApiLocaleResult.success(reportFacade.replace(dto));
  }

  @Operation(summary = "Generate report immediately", description = "Trigger immediate generation of a report without waiting for scheduled execution", operationId = "report:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report generation started successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")
  })
  @PostMapping("/{id}/generate/now")
  public ApiLocaleResult<?> generateNow(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long id) {
    reportFacade.generateNow(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete reports", description = "Batch delete specified reports and their associated configurations", operationId = "report:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Reports deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "List of report IDs", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    reportFacade.delete(ids);
  }

  /*@Operation(summary = "Query the share token of report", operationId = "report:share:token:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/shareToken")
  public ApiLocaleResult<String> shareToken(
      @Parameter(name = "id", description = "Report id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(reportFacade.shareToken(id));
  }*/

  @Operation(summary = "Get report content", description = "Retrieve the current content of a specific report", operationId = "report:content:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")})
  @GetMapping(value = "/{id}/content")
  public ApiLocaleResult<Object> content(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(reportFacade.content(id));
  }

  @Operation(summary = "Get task report content", description = "Retrieve report content specifically for a task, including task details and progress", operationId = "report:content:task:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task report content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Task not found")})
  @GetMapping(value = "/content/task/{taskId}")
  public ApiLocaleResult<TaskContentVo> taskContent(
      @Parameter(name = "taskId", description = "Task ID", required = true) @PathVariable("taskId") Long taskId) {
    return ApiLocaleResult.success(reportFacade.taskContent(taskId));
  }

  @Operation(summary = "Get functional case report content", description = "Retrieve report content specifically for a functional test case", operationId = "report:content:funcCase:query")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional case report content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional case not found")})
  @GetMapping(value = "/content/funcCase/{caseId}")
  public ApiLocaleResult<FuncCaseContentVo> funcCaseContent(
      @Parameter(name = "caseId", description = "Functional case ID", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(reportFacade.funcCaseContent(caseId));
  }

  @Operation(summary = "Get report details", description = "Retrieve comprehensive configuration and metadata information of a specific report", operationId = "report:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ReportDetailVo> detail(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(reportFacade.detail(id));
  }

  @Operation(summary = "List reports", description = "Retrieve paginated list of reports with filtering and sorting support", operationId = "report:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reports list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ReportListVo>> list(@Valid @ParameterObject ReportFindDto dto) {
    return ApiLocaleResult.success(reportFacade.list(dto));
  }

  @Operation(summary = "Search reports", description = "Full-text search reports based on keywords with advanced filtering options", operationId = "report:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reports search completed successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ReportListVo>> search(
      @Valid @ParameterObject ReportSearchDto dto) {
    return ApiLocaleResult.success(reportFacade.search(dto));
  }

}
