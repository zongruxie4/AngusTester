package cloud.xcan.angus.core.tester.interfaces.report;


import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportFacade;
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

@Tag(name = "Report Public Api", description = "Public Report Access - "
    + "Provides public access to report content through share tokens, "
    + "enabling external stakeholders to view reports without authentication")
@Validated
@RestController
@RequestMapping("/pubapi/v1/report")
@Conditional(NotCommunityEditionCondition.class)
public class ReportPubRest {

  @Resource
  private ReportFacade reportFacade;

  @Operation(summary = "Access public report content", description = "Retrieve report content using a valid share token, supporting both current and historical record versions", operationId = "report:content:query:pub")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Public report content accessed successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found or invalid token")})
  @GetMapping(value = "/{id}/content")
  public ApiLocaleResult<Object> content(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long id,
      @Parameter(name = "token", description = "Share token for public access", required = true) @RequestParam("token") String token,
      @Parameter(name = "recordId", description = "Optional report record ID for specific version") @RequestParam(value = "recordId", required = false) Long recordId) {
    return ApiLocaleResult.successData(reportFacade.content(id, token, recordId));
  }

}
