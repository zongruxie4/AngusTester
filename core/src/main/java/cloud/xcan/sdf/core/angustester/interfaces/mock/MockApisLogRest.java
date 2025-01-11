package cloud.xcan.sdf.core.angustester.interfaces.mock;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisLogFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "MockApisLog")
@Validated
@RestController
@RequestMapping("/api/v1/mock")
public class MockApisLogRest {

  @Resource
  private MockApisLogFacade mockApisLogFacade;

  @ApiOperation(value = "Query the request log detail of mock apis", nickname = "mock:apis:log:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/apis/log/{id}")
  public ApiLocaleResult<MockApisLogDetailVo> detail(
      @ApiParam(name = "id", value = "Mock apis log id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisLogFacade.detail(id));
  }

  @ApiOperation(value = "Query the request log list of mock apis", nickname = "mock:service:apis:log:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/service/{mockServiceId}/apis/log")
  public ApiLocaleResult<PageResult<MockApisLogListVo>> list(
      @ApiParam(name = "mockServiceId", value = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Valid MockApisLogFindDto dto) {
    return ApiLocaleResult.success(mockApisLogFacade.list(mockServiceId, dto));
  }

  @ApiOperation(value = "Query the request log list of mock apis", nickname = "mock:service:apis:log:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/service/{mockServiceId}/apis/log/search")
  public ApiLocaleResult<PageResult<MockApisLogListVo>> search(
      @ApiParam(name = "mockServiceId", value = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Valid MockApisLogSearchDto dto) {
    return ApiLocaleResult.success(mockApisLogFacade.search(mockServiceId, dto));
  }
}