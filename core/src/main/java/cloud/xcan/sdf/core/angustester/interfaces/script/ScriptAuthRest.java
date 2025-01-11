package cloud.xcan.sdf.core.angustester.interfaces.script;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.ScriptAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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

@Api(tags = "ScriptAuth")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptAuthRest {

  @Resource
  private ScriptAuthFacade scriptAuthFacade;

  @ApiOperation(value = "Add the authorization of script", nickname = "script:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "script id", required = true) @PathVariable("id") Long scriptId,
      @Valid @RequestBody ScriptAuthAddDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.add(scriptId, dto));
  }

  @ApiOperation(value = "Replace the authorization of script", nickname = "script:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Script authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScriptAuthReplaceDto dto) {
    scriptAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of script", nickname = "script:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long scriptId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scriptAuthFacade.enabled(scriptId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of script", nickname = "script:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long scriptId) {
    return ApiLocaleResult.success(scriptAuthFacade.status(scriptId));
  }

  @ApiOperation(value = "Delete the authorization of script", nickname = "script:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Script authorization id", required = true) @PathVariable("id") Long id) {
    scriptAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission", nickname = "script:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScriptPermission>> userAuth(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long scriptId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(scriptAuthFacade.userAuth(scriptId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission", nickname = "script:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScriptAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long scriptId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuth(scriptId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission", nickname = "script:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, ScriptAuthCurrentVo>> currentUserAuths(
      @ApiParam(name = "scriptIds", value = "Script ids", required = true) @RequestParam(value = "scriptIds") @NotEmpty HashSet<Long> scriptIds,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuths(scriptIds, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of script, the administrator permission is included", nickname = "script:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long scriptId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Script authorized permission", required = true) @PathVariable("authPermission") ScriptPermission permission) {
    scriptAuthFacade.authCheck(scriptId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of script authorization", nickname = "script:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScriptAuthVo>> list(@Valid ScriptAuthFindDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.list(dto));
  }

}
