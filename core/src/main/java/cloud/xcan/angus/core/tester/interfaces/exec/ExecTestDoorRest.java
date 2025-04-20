package cloud.xcan.angus.core.tester.interfaces.exec;


import cloud.xcan.angus.api.tester.exec.dto.TestResultUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecTestFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ExecTest")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec")
public class ExecTestDoorRest {

  @Resource
  private ExecTestFacade execTestFacade;

  @Operation(description = "Update the test results of execution", operationId = "exec:test:result:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @PatchMapping(value = "/test/result")
  public ApiLocaleResult<?> testResultUpdate(@RequestBody TestResultUpdateDto dto) {
    execTestFacade.testResultUpdate(dto);
    return ApiLocaleResult.success();
  }

}
