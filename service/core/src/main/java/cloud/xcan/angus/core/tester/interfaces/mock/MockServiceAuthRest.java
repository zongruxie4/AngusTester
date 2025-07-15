package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "MockServiceAuth", description = "Mock Service Access Control - Manage data permissions and authorization policies for mock services")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceAuthRest {

  @Resource
  private MockServiceAuthFacade mockServiceAuthFacade;

  @Operation(summary = "Add the authorization of mock service", operationId = "mock:service:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Valid @RequestBody ServiceAddAuthDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.add(serviceId, dto));
  }

  @Operation(summary = "Replace the authorization of mock service", operationId = "mock:service:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Mock service authorization id", required = true) @PathVariable("id") Long id,
      @RequestBody ServiceAuthReplaceDto dto) {
    mockServiceAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of mock service", operationId = "mock:service:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    mockServiceAuthFacade.enabled(serviceId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of mock service", operationId = "mock:service:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(mockServiceAuthFacade.status(serviceId));
  }

  @Operation(summary = "Delete the authorization of mock service", operationId = "mock:service:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Mock service authorization id", required = true) @PathVariable("id") Long id) {
    mockServiceAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permission of mock datasource", operationId = "mock:service:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<MockServicePermission>> userAuth(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(mockServiceAuthFacade.userAuth(serviceId, userId, admin));
  }

  @Operation(summary = "Check the user authorization permission of mock service", operationId = "mock:service:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Mock service authorized permission", required = true) @PathVariable("authPermission") MockServicePermission permission) {
    mockServiceAuthFacade.authCheck(serviceId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the authorization list of mock service", operationId = "mock:service:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{id}/auth")
  public ApiLocaleResult<PageResult<ServiceAuthVo>> list(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ServiceAuthFindDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.list(id, dto));
  }

}
