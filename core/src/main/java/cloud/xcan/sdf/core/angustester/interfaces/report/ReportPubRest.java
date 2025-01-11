package cloud.xcan.sdf.core.angustester.interfaces.report;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportFacade;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
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

@Api(tags = "ReportPub")
@Validated
@RestController
@RequestMapping("/pubapi/v1/report")
@Conditional(NotPrivateEditionCondition.class)
public class ReportPubRest {

  @Resource
  private ReportFacade reportFacade;

  @ApiOperation(value = "Query the content of report", nickname = "report:content:query:pub")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/content")
  public ApiLocaleResult<Object> content(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "token", value = "Share token", required = true) @RequestParam("token") String token,
      @ApiParam(name = "recordId", value = "Report record id") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.successData(reportFacade.content(id, token, recordId));
  }

}
