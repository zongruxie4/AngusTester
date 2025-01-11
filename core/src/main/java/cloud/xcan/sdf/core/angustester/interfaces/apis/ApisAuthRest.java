package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
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

@Api(tags = "ApisAuth")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisAuthRest {

  @Resource
  private ApisAuthFacade apisAuthFacade;

  @ApiOperation(value = "Add the authorization of http or websocket apis", nickname = "apis:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @RequestBody ApisAuthAddDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.add(apiId, dto));
  }

  @ApiOperation(value = "Replace the authorization of http or websocket apis", nickname = "apis:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Apis authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAuthReplaceDto dto) {
    apisAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of http or websocket apis", nickname = "apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisAuthFacade.enabled(apiId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of http or websocket apis", nickname = "apis:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId) {
    return ApiLocaleResult.success(apisAuthFacade.status(apiId));
  }

  @ApiOperation(value = "Delete the authorization of http or websocket apis", nickname = "apis:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Apis authorization id", required = true) @PathVariable("id") Long id) {
    apisAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of http or websocket apis and throw 404 when apis doesn't exist", nickname = "apis:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ApiPermission>> userAuth(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId,
      @PathVariable("userId") @ApiParam(name = "userId", value = "userId", required = true) Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(apisAuthFacade.userAuth(apiId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of http or websocket apis and throw 404 when apis doesn't exist", nickname = "apis:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ApisAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(apisAuthFacade.currentUserAuth(apiId, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of http or websocket apis, the administrator permission is included", nickname = "apis:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apiId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Apis authorized permission", required = true) @PathVariable("authPermission") ApiPermission permission) {
    apisAuthFacade.authCheck(apiId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of http or websocket apis authorization", nickname = "apis:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ApiAuthVo>> list(@Valid ApisAuthFindDto dto) {
    return ApiLocaleResult.success(apisAuthFacade.list(dto));
  }

}