package cloud.xcan.sdf.model.remoting;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.sdf.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.sdf.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.sdf.model.remoting.vo.MockApisServiceInfoVo;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;

public interface MockApisOpen2pRemote {

  @ApiOperation(value = "Query the list of mock service", nickname = "mock:service:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @Headers("Authorization: Bearer {token}")
  @RequestLine("GET /openapi2p/v1/mock/service/{id}")
  ApiLocaleResult<MockApisServiceInfoVo> mockService(@Param("token") String token,
      @Param("id") Long id);

  @ApiOperation(value = "Query the list of mock apis", nickname = "mock:apis:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @Headers("Authorization: Bearer {token}")
  @RequestLine("GET /openapi2p/v1/mock/apis")
  ApiLocaleResult<List<MockApisInfoVo>> mockApis(@Param("token") String token,
      @QueryMap MockApisDetailDto dto);

  // Note: PATCH method is not supported by @RequestLine
  @ApiOperation(value = "Update the request counter of mock apis", nickname = "mock:apis:counter:update:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
  @RequestLine("POST /openapi2p/v1/mock/apis/counter")
  ApiLocaleResult<?> counterUpdate(@Param("token") String token, MockApisRequestCountDto dto);
}
