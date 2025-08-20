package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisLogFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
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

@Tag(name = "Mock Apis Log", description = "Mock Log Analytics and Query - Query and analysis of mock interface request logs with statistical metrics including success/failure rates and latency distributions")
@Validated
@RestController
@RequestMapping("/api/v1/mock")
public class MockApisLogRest {

  @Resource
  private MockApisLogFacade mockApisLogFacade;

  @Operation(summary = "Query mock API request log details",
      description = "Retrieve detailed information about specific mock API request logs for debugging and analysis",
      operationId = "mock:apis:log:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API request log details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API request log not found")})
  @GetMapping(value = "/apis/log/{id}")
  public ApiLocaleResult<MockApisLogDetailVo> detail(
      @Parameter(name = "id", description = "Mock API request log identifier for precise query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisLogFacade.detail(id));
  }

  @Operation(summary = "Query mock API request logs with filtering",
      description = "Retrieve paginated list of mock API request logs with filtering and search capabilities",
      operationId = "mock:service:apis:log:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API request logs retrieved successfully")})
  @GetMapping("/service/{mockServiceId}/apis/log")
  public ApiLocaleResult<PageResult<MockApisLogListVo>> list(
      @Parameter(name = "mockServiceId", description = "Mock service identifier for log scope filtering", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Valid @ParameterObject MockApisLogFindDto dto) {
    return ApiLocaleResult.success(mockApisLogFacade.list(mockServiceId, dto));
  }

}
