package cloud.xcan.angus.api.tester.scenario;


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
public interface ScenarioTestRemote {

  @Operation(description = "Find enabled functionality, performance, stability testing type of scenario.", operationId = "scenario:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/api/v1/scenario/{id}/test/enabled")
  ApiLocaleResult<List<TestType>> testEnabledFind(@Parameter(name = "id", required = true) @PathVariable("id") Long scenarioId);
}
