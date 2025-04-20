package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.api.pojo.ApisRequestLog;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisLogOpen2pFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author XiaoLong Liu
 */
@Tag(name = "MockApisLogOpen2p", description = "Upload Mock Request Logs - Internal endpoints for storing mock request/response logs within AngusMockService.")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock")
public class MockApisLogOpen2pRest {

  @Resource
  private MockApisLogOpen2pFacade mockApisLogOpen2pFacade;

  @Operation(description = "Add the request logs of mock apis", operationId = "mock:apis:log:add:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @PostMapping(value = "/apis/log")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody List<ApisRequestLog> dto) {
    return ApiLocaleResult.success(mockApisLogOpen2pFacade.add(dto));
  }

}
