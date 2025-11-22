package cloud.xcan.angus.core.tester.interfaces.report;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportRecordFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.record.ReportRecordFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report Record", description = "Report Generation Records - "
    + "Provides a unified access point for managing records of generated reports, "
    + "tracking report versions and generation history")
@Validated
@RestController
@RequestMapping("/api/v1/report/record")
@Conditional(NotCommunityEditionCondition.class)
public class ReportRecordRest {

  @Resource
  private ReportRecordFacade reportRecordFacade;

  @Operation(summary = "Delete report records", description = "Batch delete specified report generation records", operationId = "report:record:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Report records deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "List of report record IDs", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    reportRecordFacade.delete(ids);
  }

  @Operation(summary = "Get report record details", description = "Retrieve detailed information of a specific report generation record", operationId = "report:record:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report record details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report record not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ReportRecordDetailVo> detail(
      @Parameter(name = "id", description = "Report record ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(reportRecordFacade.detail(id));
  }

  @Operation(summary = "List report records", description = "Retrieve paginated list of report generation records with filtering support", operationId = "report:record:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report records list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ReportRecordListVo>> list(
      @Valid @ParameterObject ReportRecordFindDto dto) {
    return ApiLocaleResult.success(reportRecordFacade.list(dto));
  }

}
