package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaolong.liu
 */
@Api(tags = "ApisSharePub")
@Validated
@RestController
@RequestMapping("/pubapi/v1/apis/share")
public class ApisSharePubRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @ApiOperation(value = "Query the sharing detail of apis", nickname = "apis:share:view:pub")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @GetMapping("/view")
  public ApiLocaleResult<ApisShareViewVo> view(@Valid ApisShareViewDto dto) {
    return ApiLocaleResult.success(apisShareFacade.view(dto));
  }

}
