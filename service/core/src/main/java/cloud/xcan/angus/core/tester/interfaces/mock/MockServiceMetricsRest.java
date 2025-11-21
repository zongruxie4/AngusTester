package cloud.xcan.angus.core.tester.interfaces.mock;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceMetricsFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.MockServiceMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
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

@Tag(name = "Mock Service Metrics", description = "Mock Service Monitoring & Metrics - Access point for mock service observability data with performance analytics and health monitoring")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceMetricsRest {

  @Resource
  private MockServiceMetricsFacade mockServiceMetricsFacade;

  @Operation(summary = "Query mock service metrics with filtering",
      description = "Retrieve mock service metrics with pagination and filtering for performance analysis and monitoring",
      operationId = "mock:service:metrics")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics")
  public ApiLocaleResult<PageResult<MockServiceMetricsVo>> metrics(
      @Parameter(name = "id", description = "Mock service identifier for metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(mockServiceMetricsFacade.metrics(id, dto));
  }

}
