package cloud.xcan.angus.model.remoting;

import cloud.xcan.angus.api.pojo.ApisRequestLog;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

public interface MockApisLogOpen2pRemote {

  @Operation(summary = "Add the request logs of mock apis", operationId = "mock:apis:log:add:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
  @RequestLine("POST /openapi2p/v1/mock/apis/log")
  ApiLocaleResult<List<IdKey<Long, Object>>> add(@Param("token") String token,
      List<ApisRequestLog> dto);

}
