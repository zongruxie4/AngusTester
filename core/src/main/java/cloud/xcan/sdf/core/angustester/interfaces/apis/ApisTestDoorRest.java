package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisTestFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ApisTestDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1/apis")
public class ApisTestDoorRest {

  @Resource
  private ApisTestFacade apisTestFacade;

  @ApiOperation(value = "Find enabled functionality, performance, stability testing type of apis.", nickname = "apis:test:enabled:find:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testEnabledFind(apisId));
  }

}
