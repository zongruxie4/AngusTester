package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisOpen2pFacade;
import cloud.xcan.angus.model.remoting.dto.MockApisDetailDto;
import cloud.xcan.angus.model.remoting.dto.MockApisRequestCountDto;
import cloud.xcan.angus.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisServiceInfoVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author XiaoLong Liu
 */
@Tag(name = "MockApisOpen2p", description = "Mock Definition Sync - Internal endpoints for querying and synchronizing mock interface definitions across AngusMockService instances.")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock")
public class MockApisOpen2pRest {

  @Resource
  private MockApisOpen2pFacade mockApisOpen2pFacade;

  @Operation(description = "Query the list of mock service", operationId = "mock:service:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/service/{id}")
  public ApiLocaleResult<MockApisServiceInfoVo> mockService(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisOpen2pFacade.mockService(id));
  }

  @Operation(description = "Query the list of mock apis", operationId = "mock:apis:detail:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/apis")
  public ApiLocaleResult<List<MockApisInfoVo>> mockApis(@Valid MockApisDetailDto dto) {
    return ApiLocaleResult.success(mockApisOpen2pFacade.mockApis(dto));
  }

  @Operation(description = "Update the request counter of mock apis", operationId = "mock:apis:counter:update:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @PostMapping(value = "/apis/counter")
  public ApiLocaleResult<?> counterUpdate(@Valid @RequestBody MockApisRequestCountDto dto) {
    mockApisOpen2pFacade.counterUpdate(dto);
    return ApiLocaleResult.success();
  }

}
