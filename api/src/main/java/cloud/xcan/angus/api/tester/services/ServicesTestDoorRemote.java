package cloud.xcan.angus.api.tester.services;


import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ServicesTestDoorRemote {

  @Operation(description = "The testing apis summary the functionality, performance, stability testing of service", operationId = "services:apis:test:count:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/innerapi/v1/services/{id}/test/apis/count")
  ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @Parameter(name = "id", required = true) @PathVariable("id") Long serviceId,
      @SpringQueryMap OrgAndDateFilterDto dto);

  @Operation(description = "The testing apis summary the functionality, performance, stability testing of project", operationId = "project:apis:test:count:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/innerapi/v1/project/{id}/test/apis/count")
  ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @Parameter(name = "id", required = true) @PathVariable("id") Long projectId,
      @SpringQueryMap OrgAndDateFilterDto dto);
}
