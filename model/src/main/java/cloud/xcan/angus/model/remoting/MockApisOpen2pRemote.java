package cloud.xcan.angus.model.remoting;

import cloud.xcan.angus.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.angus.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.angus.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisServiceInfoVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

public interface MockApisOpen2pRemote {

  @Operation(description = "Query the list of mock service", operationId = "mock:service:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @Headers("Authorization: Bearer {token}")
  @RequestLine("GET /openapi2p/v1/mock/service/{id}")
  ApiLocaleResult<MockApisServiceInfoVo> mockService(@Param("token") String token,
      @Param("id") Long id);

  @Operation(description = "Query the list of mock apis", operationId = "mock:apis:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @Headers("Authorization: Bearer {token}")
  @RequestLine("GET /openapi2p/v1/mock/apis")
  ApiLocaleResult<List<MockApisInfoVo>> mockApis(@Param("token") String token,
      @QueryMap MockApisDetailDto dto);

  // Note: PATCH method is not supported by @RequestLine
  @Operation(description = "Update the request counter of mock apis", operationId = "mock:apis:counter:update:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
  @RequestLine("POST /openapi2p/v1/mock/apis/counter")
  ApiLocaleResult<?> counterUpdate(@Param("token") String token, MockApisRequestCountDto dto);
}
