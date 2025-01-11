package cloud.xcan.sdf.core.angustester.interfaces.exec;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.exec.dto.TestResultUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.exec.facade.ExecTestFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ExecTest")
@Validated
@RestController
@RequestMapping("/doorapi/v1/exec")
public class ExecTestDoorRest {

  @Resource
  private ExecTestFacade execTestFacade;

  @ApiOperation(value = "Update the test results of execution", nickname = "exec:test:result:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @PatchMapping(value = "/test/result")
  public ApiLocaleResult<?> testResultUpdate(@RequestBody TestResultUpdateDto dto) {
    execTestFacade.testResultUpdate(dto);
    return ApiLocaleResult.success();
  }

}