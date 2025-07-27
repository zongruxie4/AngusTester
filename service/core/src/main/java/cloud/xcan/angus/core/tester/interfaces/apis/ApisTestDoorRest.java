package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTestFacade;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "APIs Internal Testing", description = "Internal APIs Testing - Service internal API testing query entrance with comprehensive test type management and internal service integration")
@Validated
@RestController
@RequestMapping("/innerapi/v1/apis")
public class ApisTestDoorRest {

  @Resource
  private ApisTestFacade apisTestFacade;

  @Operation(summary = "Get enabled API test types", 
      description = "Retrieve enabled testing types for specific API with comprehensive test configuration information",
      operationId = "apis:test:enabled:find:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled API test types retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @Parameter(name = "id", description = "API identifier for test type query", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testEnabledFind(apisId));
  }

}
