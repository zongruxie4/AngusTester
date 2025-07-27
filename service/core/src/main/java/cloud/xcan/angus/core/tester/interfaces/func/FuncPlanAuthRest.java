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

@Tag(name = "Functional Test Plan Authorization", description = "Functional Test Plan Authorization Management - Comprehensive APIs for managing data access permissions of test plans, their linked cases, and baseline references with granular permission control")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanAuthRest {

  @Resource
  private FuncPlanAuthFacade funcPlanAuthFacade;

  @Operation(summary = "Add test plan authorization", 
      description = "Create authorization for a specific functional test plan with comprehensive permission settings",
      operationId = "func:plan:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test plan authorization created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Test plan identifier for authorization creation", required = true) @PathVariable("id") Long planId,
      @Valid @RequestBody FuncPlanAuthAddDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.add(planId, dto));
  }

  @Operation(summary = "Replace test plan authorization", 
      description = "Update authorization settings for a specific functional test plan with complete permission replacement",
      operationId = "func:plan:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test plan authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Authorization identifier for replacement operation", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncPlanAuthReplaceDto dto) {
    funcPlanAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable test plan authorization", 
      description = "Toggle authorization status for a specific functional test plan to control access",
      operationId = "func:plan:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan authorization status updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Test plan identifier for authorization status update", required = true) @PathVariable("id") Long planId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Authorization enablement flag for access control", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    funcPlanAuthFacade.enabled(planId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get test plan authorization status", 
      description = "Retrieve authorization status for a specific functional test plan",
      operationId = "func:plan:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test plan not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Test plan identifier for status retrieval", required = true) @PathVariable("id") Long planId) {
    return ApiLocaleResult.success(funcPlanAuthFacade.status(planId));
  }

  @Operation(summary = "Delete test plan authorization", 
      description = "Remove authorization for a specific functional test plan to revoke access",
      operationId = "func:plan:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test plan authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Authorization identifier for deletion", required = true) @PathVariable("id") Long id) {
    funcPlanAuthFacade.delete(id);
  }

  @Operation(summary = "Get user authorization permissions", 
      description = "Retrieve authorization permissions for a specific user on a functional test plan",
      operationId = "func:plan:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test plan not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<FuncPlanPermission>> userAuth(
      @Parameter(name = "id", description = "Test plan identifier for permission query", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "userId", description = "User identifier for permission query", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Administrator permission flag for enhanced access control") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.userAuth(planId, userId, admin));
  }

  @Operation(summary = "Get current user authorization permissions", 
      description = "Retrieve authorization permissions for the current user on a functional test plan",
      operationId = "func:plan:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user authorization permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test plan not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<FuncPlanAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Test plan identifier for current user permission query", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "admin", description = "Administrator permission flag for enhanced access control") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuth(planId, admin));
  }

  @Operation(summary = "Get current user authorization permissions for multiple plans", 
      description = "Retrieve authorization permissions for the current user on multiple functional test plans",
      operationId = "func:plan:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user authorization permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test plan not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, FuncPlanAuthCurrentVo>> currentUserAuths(
      @Parameter(name = "ids", description = "Test plan identifiers for batch permission query", required = true) @RequestParam(value = "ids") @NotEmpty HashSet<Long> planIds,
      @Parameter(name = "admin", description = "Administrator permission flag for enhanced access control") Boolean admin) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuths(planIds, admin));
  }

  @Operation(summary = "Check user authorization permission", 
      description = "Verify specific authorization permission for a user on a functional test plan",
      operationId = "func:plan:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization permission verified successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Test plan identifier for permission verification", required = true) @PathVariable("id") Long planId,
      @Parameter(name = "userId", description = "User identifier for permission verification", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Specific permission to verify for access control", required = true) @PathVariable("authPermission") FuncPlanPermission permission) {
    funcPlanAuthFacade.authCheck(planId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "List test plan authorizations", 
      description = "Retrieve paginated list of test plan authorizations with comprehensive filtering and search options",
      operationId = "func:plan:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test plan authorization list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<FuncPlanAuthVo>> list(
      @Valid @ParameterObject FuncPlanAuthFindDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.list(dto));
  }

}
