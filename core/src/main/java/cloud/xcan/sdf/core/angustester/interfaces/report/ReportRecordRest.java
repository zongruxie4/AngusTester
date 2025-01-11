package cloud.xcan.sdf.core.angustester.interfaces.report;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportRecordFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.record.ReportRecordFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.record.ReportRecordDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.record.ReportRecordListVo;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "ReportRecord")
@Validated
@RestController
@RequestMapping("/api/v1/report/record")
@Conditional(NotPrivateEditionCondition.class)
public class ReportRecordRest {

  @Resource
  private ReportRecordFacade reportRecordFacade;

  @ApiOperation(value = "Delete report record", nickname = "report:record:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Task ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    reportRecordFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of report record", nickname = "report:record:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ReportRecordDetailVo> detail(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(reportRecordFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of report record", nickname = "report:record:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ReportRecordListVo>> list(@Valid ReportRecordFindDto dto) {
    return ApiLocaleResult.success(reportRecordFacade.list(dto));
  }

}
