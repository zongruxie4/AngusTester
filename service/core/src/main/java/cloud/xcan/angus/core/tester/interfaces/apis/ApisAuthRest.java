package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
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
import jakarta.validation.constraints.NotNull;
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

@Tag(name = "ApisAuth", description = "API Authorization Management - Centralized control for user data access permissions")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisAuthRest {

  @Resource
  private ApisAuthFacade apisAuthFacade;

  @Operation(summary = "Add the authorization of api", operationId = "apis:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @RequestBody ApisAuthAddDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.add(apiId, dto));
  }

  @Operation(summary = "Replace the authorization of api", operationId = "apis:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Apis authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAuthReplaceDto dto) {
    apisAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of api", operationId = "apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled or Disabled", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisAuthFacade.enabled(apiId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of api", operationId = "apis:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId) {
    return ApiLocaleResult.success(apisAuthFacade.status(apiId));
  }

  @Operation(summary = "Delete the authorization of api", operationId = "apis:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Apis authorization id", required = true) @PathVariable("id") Long id) {
    apisAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permissions of api", operationId = "apis:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ApiPermission>> userAuth(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @PathVariable("userId") @Parameter(name = "userId", description = "userId", required = true) Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(apisAuthFacade.userAuth(apiId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permissions of api", operationId = "apis:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ApisAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(apisAuthFacade.currentUserAuth(apiId, admin));
  }

  @Operation(summary = "Check the user authorization or administrator permission of api", operationId = "apis:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Apis authorized permission", required = true) @PathVariable("authPermission") ApiPermission permission) {
    apisAuthFacade.authCheck(apiId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of api authorization", operationId = "apis:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ApiAuthVo>> list(@Valid @ParameterObject ApisAuthFindDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.list(dto));
  }

}
