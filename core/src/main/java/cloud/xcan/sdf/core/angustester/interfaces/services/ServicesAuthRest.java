package cloud.xcan.sdf.core.angustester.interfaces.services;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthVo;
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

@Api(tags = "ServicesAuth")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesAuthRest {

  @Resource
  private ServicesAuthFacade servicesAuthFacade;

  @ApiOperation(value = "Add the authorization of services", nickname = "services:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @RequestBody ServicesAddAuthDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.add(projectId, dto));
  }

  @ApiOperation(value = "Replace the authorization of services", nickname = "services:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "services authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ServicesAuthReplaceDto dto) {
    servicesAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the authorization of services", nickname = "services:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "services authorization id", required = true) @PathVariable("id") Long id) {
    servicesAuthFacade.delete(id);
  }

  @ApiOperation(value = "Enable or disable the authorization of services", nickname = "services:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.enabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of services", nickname = "services:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId) {
    return ApiLocaleResult.success(servicesAuthFacade.status(projectId));
  }

  @ApiOperation(value = "Enable or disable the authorization of services apis", nickname = "services:apis:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/apis/auth/enabled")
  public ApiLocaleResult<?> apisEnabled(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesAuthFacade.apisEnabled(projectId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the user authorization permission of services and throw 404 when services doesn't exist", nickname = "services:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ServicesPermission>> userAuth(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(servicesAuthFacade.userAuth(projectId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of services and throw 404 when services doesn't exist", nickname = "services:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ServiceAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(servicesAuthFacade.currentUserAuth(projectId, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of services, the administrator permission is included", nickname = "services:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "services id", required = true) @PathVariable("id") Long projectId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Services authorized permission", required = true) @PathVariable("authPermission") ServicesPermission authPermission) {
    servicesAuthFacade.authCheck(projectId, authPermission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of services authorization", nickname = "services:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ServicesAuthVo>> list(@Valid ServicesAuthFindDto dto) {
    return ApiLocaleResult.success(servicesAuthFacade.list(dto));
  }

}
