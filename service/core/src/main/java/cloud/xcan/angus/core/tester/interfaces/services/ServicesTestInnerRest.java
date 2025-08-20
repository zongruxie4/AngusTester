package cloud.xcan.angus.core.tester.interfaces.services;


import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Services Test - Internal", description = "API Test and Analytics Internal API - Internal query endpoints for API test execution statistics including success rates and latency percentiles")
@Validated
@RestController
@RequestMapping("/innerapi/v1")
public class ServicesTestInnerRest {

  @Resource
  private ServicesTestFacade servicesTestFacade;

  @Operation(summary = "Query service API testing count",
      description = "Retrieve API testing execution statistics for service performance analysis",
      operationId = "services:test:apis:count:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service API testing count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/services/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @Parameter(name = "id", description = "Service identifier for testing statistics query", required = true) @PathVariable("id") Long serviceId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countServiceTestApis(serviceId, dto));
  }

  @Operation(summary = "Query project API testing count",
      description = "Retrieve API testing execution statistics for project performance analysis",
      operationId = "project:test:apis:count:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project API testing count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @Parameter(name = "id", description = "Project identifier for testing statistics query", required = true) @PathVariable("id") Long projectId,
      @ParameterObject OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countProjectTestApis(projectId, dto));
  }

}
