package cloud.xcan.sdf.model.remoting;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.pojo.ApisRequestLog;
import cloud.xcan.sdf.spec.experimental.IdKey;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;

public interface MockApisLogOpen2pRemote {

  @ApiOperation(value = "Add the request logs of mock apis", nickname = "mock:apis:log:add:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
  @RequestLine("POST /openapi2p/v1/mock/apis/log")
  ApiLocaleResult<List<IdKey<Long, Object>>> add(@Param("token") String token,
      List<ApisRequestLog> dtos);

}
