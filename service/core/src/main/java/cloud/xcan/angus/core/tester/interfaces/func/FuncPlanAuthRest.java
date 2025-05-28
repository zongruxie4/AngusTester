package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncPlanAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth.FuncPlanAuthVo;
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

@Tag(name = "FuncPlanAuth", description = "Test Plan Authorization Management - Unified entry for managing data access permissions of test plans, their linked cases, and baseline references.")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanAuthRest {

  @Resource
  private FuncPlanAuthFacade funcPlanAuthFacade;

  @Operation(summary = "Add the authorization of functional test plan", operationId = "func:plan:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Valid @RequestBody FuncPlanAuthAddDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.add(planId, dto));
  }

  @Operation(summary = "Replace the authorization of functional test plan", operationId = "func:plan:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Functional test plan authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncPlanAuthReplaceDto dto) {
    funcPlanAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of functional test plan", operationId = "func:plan:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    funcPlanAuthFacade.enabled(planId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of functional test plan", operationId = "func:plan:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId) {
    return ApiLocaleResult.success(funcPlanAuthFacade.status(planId));
  }

  @Operation(summary = "Delete the authorization of functional test plan", operationId = "func:plan:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Functional test plan authorization id", required = true) @PathVariable("id") Long id) {
    funcPlanAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permission of functional test plan", operationId = "func:plan:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<FuncPlanPermission>> userAuth(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.userAuth(planId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permission of functional test plan", operationId = "func:plan:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<FuncPlanAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuth(planId, admin));
  }

  @Operation(summary = "Query the current user authorization permission", operationId = "func:plan:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, FuncPlanAuthCurrentVo>> currentUserAuths(
      @Parameter(name = "ids", description = "Functional test plan ids", required = true) @RequestParam(value = "ids") @NotEmpty HashSet<Long> planIds,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuths(planIds, admin));
  }

  @Operation(summary = "Check the user authorization permission of functional test plan, the administrator permission is included", operationId = "func:plan:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Function plan authorized permission", required = true) @PathVariable("authPermission") FuncPlanPermission permission) {
    funcPlanAuthFacade.authCheck(planId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of functional test plan authorization", operationId = "func:plan:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<FuncPlanAuthVo>> list(
      @Valid @ParameterObject FuncPlanAuthFindDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.list(dto));
  }

}
