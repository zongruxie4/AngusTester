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

@Tag(name = "Mock Service Authorization", description = "Mock Service Access Control - Management of data permissions and authorization policies for mock services with granular access control")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceAuthRest {

  @Resource
  private MockServiceAuthFacade mockServiceAuthFacade;

  @Operation(summary = "Add mock service authorization",
      description = "Create new authorization policy for mock service with permission management",
      operationId = "mock:service:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock service authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Mock service identifier for authorization assignment", required = true) @PathVariable("id") Long serviceId,
      @Valid @RequestBody ServiceAddAuthDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.add(serviceId, dto));
  }

  @Operation(summary = "Replace mock service authorization",
      description = "Update existing authorization policy for mock service with permission modifications",
      operationId = "mock:service:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Mock service authorization identifier for replacement", required = true) @PathVariable("id") Long id,
      @RequestBody ServiceAuthReplaceDto dto) {
    mockServiceAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable mock service authorization",
      description = "Toggle authorization status for mock service to control access availability",
      operationId = "mock:service:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service authorization status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Mock service identifier for authorization status control", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "enabled", description = "Authorization status flag for enable/disable control", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    mockServiceAuthFacade.enabled(serviceId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query mock service authorization status",
      description = "Retrieve current authorization status for mock service to verify access control state",
      operationId = "mock:service:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Mock service identifier for status query", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(mockServiceAuthFacade.status(serviceId));
  }

  @Operation(summary = "Delete mock service authorization",
      description = "Remove authorization policy from mock service with cleanup",
      operationId = "mock:service:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Mock service authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Mock service authorization identifier for deletion", required = true) @PathVariable("id") Long id) {
    mockServiceAuthFacade.delete(id);
  }

  @Operation(summary = "Query user authorization permissions for mock service",
      description = "Retrieve user authorization permissions for mock service with administrative override support",
      operationId = "mock:service:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service or user not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<MockServicePermission>> userAuth(
      @Parameter(name = "id", description = "Mock service identifier for permission query", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "userId", description = "User identifier for permission query", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Administrative permission flag for enhanced access control") Boolean admin) {
    return ApiLocaleResult.success(mockServiceAuthFacade.userAuth(serviceId, userId, admin));
  }

  @Operation(summary = "Check user authorization permission for mock service",
      description = "Verify specific authorization permission for user on mock service with validation",
      operationId = "mock:service:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization permission verified successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Mock service identifier for permission verification", required = true) @PathVariable("id") Long serviceId,
      @Parameter(name = "userId", description = "User identifier for permission verification", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Specific authorization permission to verify", required = true) @PathVariable("authPermission") MockServicePermission permission) {
    mockServiceAuthFacade.authCheck(serviceId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query mock service authorization list",
      description = "Retrieve paginated list of authorization policies for mock service with filtering",
      operationId = "mock:service:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service authorization list retrieved successfully")})
  @GetMapping("/{id}/auth")
  public ApiLocaleResult<PageResult<ServiceAuthVo>> list(
      @Parameter(name = "id", description = "Mock service identifier for authorization list query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ServiceAuthFindDto dto) {
    return ApiLocaleResult.success(mockServiceAuthFacade.list(id, dto));
  }

}
