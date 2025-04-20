package cloud.xcan.angus.api.tester.exec;


import cloud.xcan.angus.api.tester.exec.dto.TestResultUpdateDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ExecTestRemote {

  @Operation(description = "Update the test results of execution", operationId = "exec:test:result:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @PatchMapping(value = "/innerapi/v1/exec/test/result")
  ApiLocaleResult<?> testResultUpdate(@RequestBody TestResultUpdateDto dto);
}
