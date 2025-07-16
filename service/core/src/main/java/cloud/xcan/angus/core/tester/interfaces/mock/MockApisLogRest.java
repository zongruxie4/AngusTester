package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisLogFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MockApisLog", description = "Mock Log Analytics and Query - Query and analyze mock interface request logs and statistical metrics (success/failure rates, latency distributions)")
@Validated
@RestController
@RequestMapping("/api/v1/mock")
public class MockApisLogRest {

  @Resource
  private MockApisLogFacade mockApisLogFacade;

  @Operation(summary = "Query the request log detail of mock apis", operationId = "mock:apis:log:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/apis/log/{id}")
  public ApiLocaleResult<MockApisLogDetailVo> detail(
      @Parameter(name = "id", description = "Mock apis log id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisLogFacade.detail(id));
  }

  @Operation(summary = "Query mock apis request logs", operationId = "mock:service:apis:log:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/service/{mockServiceId}/apis/log")
  public ApiLocaleResult<PageResult<MockApisLogListVo>> list(
      @Parameter(name = "mockServiceId", description = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Valid @ParameterObject MockApisLogFindDto dto) {
    return ApiLocaleResult.success(mockApisLogFacade.list(mockServiceId, dto));
  }

  @Operation(summary = "Search mock apis request logs", operationId = "mock:service:apis:log:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/service/{mockServiceId}/apis/log/search")
  public ApiLocaleResult<PageResult<MockApisLogListVo>> search(
      @Parameter(name = "mockServiceId", description = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Valid @ParameterObject MockApisLogSearchDto dto) {
    return ApiLocaleResult.success(mockApisLogFacade.search(mockServiceId, dto));
  }
}
