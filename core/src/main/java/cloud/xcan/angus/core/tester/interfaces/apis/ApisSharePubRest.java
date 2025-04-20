package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author XiaoLong Liu
 */
@Api(tags = "ApisSharePub")
@Validated
@RestController
@RequestMapping("/pubapi/v1/apis/share")
public class ApisSharePubRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @Operation(description = "Query the sharing detail of apis", operationId = "apis:share:view:pub")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Sharing does not exist")})
  @GetMapping("/view")
  public ApiLocaleResult<ApisShareViewVo> view(@Valid ApisShareViewDto dto) {
    return ApiLocaleResult.success(apisShareFacade.view(dto));
  }

}
