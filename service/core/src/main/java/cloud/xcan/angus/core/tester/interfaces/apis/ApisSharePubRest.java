package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "APIs Sharing Public Access", description = "APIs Sharing Public Access - Public access entry for shared service and API resources with query capabilities")
@Validated
@RestController
@RequestMapping("/pubapi/v1/apis/share")
public class ApisSharePubRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @Operation(summary = "Get shared API details",
      description = "Retrieve comprehensive shared API details with public access and comprehensive resource information",
      operationId = "apis:share:view:pub")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Shared API details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Shared API not found")})
  @GetMapping("/view")
  public ApiLocaleResult<ApisShareViewVo> view(@Valid @ParameterObject ApisShareViewDto dto) {
    return ApiLocaleResult.success(apisShareFacade.view(dto));
  }

}
