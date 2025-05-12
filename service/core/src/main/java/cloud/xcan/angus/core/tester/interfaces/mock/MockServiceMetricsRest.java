package cloud.xcan.angus.core.tester.interfaces.mock;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceMetricsFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.MockServiceMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeMetricsFindDto;
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

@Tag(name = "MockServiceMetrics", description = "Mock Service Monitoring & Metrics API - Access point for mock service observability data")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceMetricsRest {

  @Resource
  private MockServiceMetricsFacade mockServiceMetricsFacade;

  @Operation(description = "Query the list of mock service sampling(JvmMemory,JvmProcessor).", operationId = "mock:sample")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics")
  public ApiLocaleResult<PageResult<MockServiceMetricsVo>> metrics(
      @Parameter(name = "id", description = "Mock service ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(mockServiceMetricsFacade.metrics(id, dto));
  }

}
