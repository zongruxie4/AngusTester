package cloud.xcan.angus.core.tester.interfaces.script;


import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthVo;
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

@Tag(name = "ScriptAuth", description = "Test Script Authorization Management - Unified entry for managing data access permissions of test script")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptAuthRest {

  @Resource
  private ScriptAuthFacade scriptAuthFacade;

  @Operation(summary = "Add the authorization of script", operationId = "script:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "script id", required = true) @PathVariable("id") Long scriptId,
      @Valid @RequestBody ScriptAuthAddDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.add(scriptId, dto));
  }

  @Operation(summary = "Replace the authorization of script", operationId = "script:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Script authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScriptAuthReplaceDto dto) {
    scriptAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of script", operationId = "script:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long scriptId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled or Disabled", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scriptAuthFacade.enabled(scriptId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of script", operationId = "script:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long scriptId) {
    return ApiLocaleResult.success(scriptAuthFacade.status(scriptId));
  }

  @Operation(summary = "Delete the authorization of script", operationId = "script:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Script authorization id", required = true) @PathVariable("id") Long id) {
    scriptAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permission", operationId = "script:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScriptPermission>> userAuth(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.userAuth(scriptId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permission", operationId = "script:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScriptAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuth(scriptId, admin));
  }

  @Operation(summary = "Query the current user authorization permission", operationId = "script:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, ScriptAuthCurrentVo>> currentUserAuths(
      @Parameter(name = "scriptIds", description = "Script ids", required = true) @RequestParam(value = "scriptIds") @NotEmpty HashSet<Long> scriptIds,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(scriptAuthFacade.currentUserAuths(scriptIds, admin));
  }

  @Operation(summary = "Check the user authorization or administrator permission of script", operationId = "script:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long scriptId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Script authorized permission", required = true) @PathVariable("authPermission") ScriptPermission permission) {
    scriptAuthFacade.authCheck(scriptId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of script authorization", operationId = "script:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScriptAuthVo>> list(
      @Valid @ParameterObject ScriptAuthFindDto dto) {
    return ApiLocaleResult.success(scriptAuthFacade.list(dto));
  }

}
