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
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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

@Api(tags = "ApisAuth")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisAuthRest {

  @Resource
  private ApisAuthFacade apisAuthFacade;

  @Operation(description = "Add the authorization of http or websocket apis", operationId = "apis:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @RequestBody ApisAuthAddDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.add(apiId, dto));
  }

  @Operation(description = "Replace the authorization of http or websocket apis", operationId = "apis:auth:replace")
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

  @Operation(description = "Enable or disable the authorization of http or websocket apis", operationId = "apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisAuthFacade.enabled(apiId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Query authorization status of http or websocket apis", operationId = "apis:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long apiId) {
    return ApiLocaleResult.success(apisAuthFacade.status(apiId));
  }

  @Operation(description = "Delete the authorization of http or websocket apis", operationId = "apis:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Apis authorization id", required = true) @PathVariable("id") Long id) {
    apisAuthFacade.delete(id);
  }

  @Operation(description = "Query the user authorization permission of http or websocket apis and throw 404 when apis doesn't exist", operationId = "apis:user:auth")
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

  @Operation(description = "Query the current user authorization permission of http or websocket apis and throw 404 when apis doesn't exist", operationId = "apis:user:auth:current")
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

  @Operation(description = "Check the user authorization permission of http or websocket apis, the administrator permission is included", operationId = "apis:auth:check")
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

  @Operation(description = "Query the list of http or websocket apis authorization", operationId = "apis:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ApiAuthVo>> list(@Valid ApisAuthFindDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.list(dto));
  }

}
