package cloud.xcan.sdf.core.angustester.interfaces.mock;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisOpen2pFacade;
import cloud.xcan.sdf.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.sdf.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.sdf.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.sdf.model.remoting.vo.MockApisServiceInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaolong.liu
 */
@Api(tags = "MockApisOpen2p")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock")
public class MockApisOpen2pRest {

  @Resource
  private MockApisOpen2pFacade mockApisOpen2pFacade;

  @ApiOperation(value = "Query the list of mock service", nickname = "mock:service:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/service/{id}")
  public ApiLocaleResult<MockApisServiceInfoVo> mockService(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisOpen2pFacade.mockService(id));
  }

  @ApiOperation(value = "Query the list of mock apis", nickname = "mock:apis:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/apis")
  public ApiLocaleResult<List<MockApisInfoVo>> mockApis(@Valid MockApisDetailDto dto) {
    return ApiLocaleResult.success(mockApisOpen2pFacade.mockApis(dto));
  }

  @ApiOperation(value = "Update the request counter of mock apis", nickname = "mock:apis:counter:update:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @PostMapping(value = "/apis/counter")
  public ApiLocaleResult<?> counterUpdate(@Valid @RequestBody MockApisRequestCountDto dto) {
    mockApisOpen2pFacade.counterUpdate(dto);
    return ApiLocaleResult.success();
  }

}
