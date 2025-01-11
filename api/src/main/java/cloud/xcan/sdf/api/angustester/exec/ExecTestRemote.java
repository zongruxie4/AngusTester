package cloud.xcan.sdf.api.angustester.exec;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.exec.dto.TestResultUpdateDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ExecTestRemote {

  @ApiOperation(value = "Update the test results of execution", nickname = "exec:test:result:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @PatchMapping(value = "/doorapi/v1/exec/test/result")
  ApiLocaleResult<?> testResultUpdate(@RequestBody TestResultUpdateDto dto);
}
