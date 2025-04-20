package cloud.xcan.angus.api.tester.apis;


import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ApisTestDoorRemote {

  @Operation(description = "Find enabled functionality, performance, stability testing type of apis.", operationId = "apis:test:enabled:find:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/innerapi/v1/apis/{id}/test/enabled")
  ApiLocaleResult<List<TestType>> testEnabledFind(@PathVariable("id") @Parameter(name = "id", required = true) Long apisId);

}
