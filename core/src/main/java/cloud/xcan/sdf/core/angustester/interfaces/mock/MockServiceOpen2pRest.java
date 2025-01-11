package cloud.xcan.sdf.core.angustester.interfaces.mock;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockServiceOpen2pFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceInfoVo;
import cloud.xcan.sdf.spec.annotations.Unused;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaolong.liu
 */
@Api(tags = "MockServiceOpen2p")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock/service")
public class MockServiceOpen2pRest {

  @Resource
  private MockServiceOpen2pFacade mockServiceOpen2pFacade;

  @Unused
  @ApiOperation(value = "Query the info of mock service", nickname = "mock:service:info:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping("/{id}/info")
  public MockServiceInfoVo info(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    return mockServiceOpen2pFacade.info(id);
  }

}
