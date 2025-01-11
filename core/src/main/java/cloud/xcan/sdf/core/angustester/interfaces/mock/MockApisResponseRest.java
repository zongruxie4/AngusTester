package cloud.xcan.sdf.core.angustester.interfaces.mock;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisResponseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "MockApisResponse")
@Validated
@RestController
@RequestMapping("/api/v1/mock/apis")
public class MockApisResponseRest {

  @Resource
  private MockApisResponseFacade mockApisResponseFacade;

  @ApiOperation(value = "Add the response of mock apis", nickname = "mock:apis:response:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apisId}/response")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @ApiParam(name = "apisId", value = "Mock apis id", required = true) @PathVariable("apisId") Long apisId,
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<MockApisResponseAddDto> dtos) {
    return ApiLocaleResult.success(mockApisResponseFacade.add(apisId, dtos));
  }

  @ApiOperation(value = "Replace the response of mock apis", nickname = "mock:apis:response:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping("/{apisId}/response")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "apisId", value = "Mock apis id", required = true) @PathVariable("apisId") Long apisId,
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<MockApisResponseReplaceDto> dtos) {
    mockApisResponseFacade.replace(apisId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the response of mock apis", nickname = "mock:apis:response:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apisId}/response")
  public void delete(
      @ApiParam(name = "apisId", value = "Mock apis id", required = true) @PathVariable("apisId") Long apisId,
      @ApiParam(name = "responseIds", value = "Mock apis response ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("responseIds") HashSet<Long> responseIds) {
    mockApisResponseFacade.delete(apisId, responseIds);
  }

  @ApiOperation(value = "Query the response of mock apis", nickname = "mock:apis:response:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{apisId}/response")
  public ApiLocaleResult<List<MockApiResponseVo>> all(
      @ApiParam(name = "apisId", value = "Mock apis id", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisResponseFacade.all(apisId));
  }

}