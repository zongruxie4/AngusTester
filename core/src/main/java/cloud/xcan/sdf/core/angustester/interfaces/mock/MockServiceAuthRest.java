package cloud.xcan.sdf.core.angustester.interfaces.mock;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockServiceAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "MockServiceAuth")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceAuthRest {

  @Resource
  private MockServiceAuthFacade mockServiceAuthFacade;

  @ApiOperation(value = "Add the authorization of mock service", nickname = "mock:service:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Valid @RequestBody ServiceAddAuthDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.add(serviceId, dto));
  }

  @ApiOperation(value = "Replace the authorization of mock service", nickname = "mock:service:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Mock service authorization id", required = true) @PathVariable("id") Long id,
      @RequestBody ServiceAuthReplaceDto dto) {
    mockServiceAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of mock service", nickname = "mock:service:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    mockServiceAuthFacade.enabled(serviceId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of mock service", nickname = "mock:service:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(mockServiceAuthFacade.status(serviceId));
  }

  @ApiOperation(value = "Delete the authorization of mock service", nickname = "mock:service:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Mock service authorization id", required = true) @PathVariable("id") Long id) {
    mockServiceAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of mock datasource", nickname = "mock:service:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<MockServicePermission>> userAuth(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(mockServiceAuthFacade.userAuth(serviceId, userId, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of mock service", nickname = "mock:service:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Mock service authorized permission", required = true) @PathVariable("authPermission") MockServicePermission permission) {
    mockServiceAuthFacade.authCheck(serviceId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the authorization list of mock service", nickname = "mock:service:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{id}/auth")
  public ApiLocaleResult<PageResult<ServiceAuthVo>> list(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id,
      @Valid ServiceAuthFindDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.list(id, dto));
  }

}