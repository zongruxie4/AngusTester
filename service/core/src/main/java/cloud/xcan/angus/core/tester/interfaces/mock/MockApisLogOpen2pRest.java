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


@Tag(name = "Mock Apis Log - Private Environment", description = "Mock Request Log Upload - Internal endpoints for storing mock request/response logs within AngusMockService with logging and analytics capabilities")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock")
public class MockApisLogOpen2pRest {

  @Resource
  private MockApisLogOpen2pFacade mockApisLogOpen2pFacade;

  @Operation(summary = "Upload mock API request logs for analytics",
      description = "Store mock API request and response logs for performance analysis and debugging purposes",
      operationId = "mock:apis:log:add:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock API request logs uploaded successfully")})
  @PostMapping(value = "/apis/log")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @RequestBody List<ApisRequestLog> dto) {
    return ApiLocaleResult.success(mockApisLogOpen2pFacade.add(dto));
  }

}
