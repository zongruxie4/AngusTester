package cloud.xcan.sdf.core.angustester.interfaces.mock;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.pojo.ApisRequestLog;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisLogOpen2pFacade;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaolong.liu
 */
@Api(tags = "MockApisLogOpen2p")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock")
public class MockApisLogOpen2pRest {

  @Resource
  private MockApisLogOpen2pFacade mockApisLogOpen2pFacade;

  @ApiOperation(value = "Add the request logs of mock apis", nickname = "mock:apis:log:add:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @PostMapping(value = "/apis/log")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody List<ApisRequestLog> dtos) {
    return ApiLocaleResult.success(mockApisLogOpen2pFacade.add(dtos));
  }

}
