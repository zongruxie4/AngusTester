package cloud.xcan.sdf.core.angustester.interfaces.func;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncPlanAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthVo;
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

@Api(tags = "FuncPlanAuth")
@Validated
@RestController
@RequestMapping("/api/v1/func/plan")
public class FuncPlanAuthRest {

  @Resource
  private FuncPlanAuthFacade funcPlanAuthFacade;

  @ApiOperation(value = "Add the authorization of functional test plan", nickname = "func:plan:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Valid @RequestBody FuncPlanAuthAddDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.add(planId, dto));
  }

  @ApiOperation(value = "Replace the authorization of functional test plan", nickname = "func:plan:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Functional test plan authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncPlanAuthReplaceDto dto) {
    funcPlanAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of functional test plan", nickname = "func:plan:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    funcPlanAuthFacade.enabled(planId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of functional test plan", nickname = "func:plan:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId) {
    return ApiLocaleResult.success(funcPlanAuthFacade.status(planId));
  }

  @ApiOperation(value = "Delete the authorization of functional test plan", nickname = "func:plan:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Functional test plan authorization id", required = true) @PathVariable("id") Long id) {
    funcPlanAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of functional test plan", nickname = "func:plan:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<FuncPlanPermission>> userAuth(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(funcPlanAuthFacade.userAuth(planId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of functional test plan", nickname = "func:plan:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<FuncPlanAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuth(planId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission", nickname = "func:plan:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, FuncPlanAuthCurrentVo>> currentUserAuths(
      @ApiParam(name = "ids", value = "Functional test plan ids", required = true) @RequestParam(value = "ids") @NotEmpty HashSet<Long> planIds,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(funcPlanAuthFacade.currentUserAuths(planIds, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of functional test plan, the administrator permission is included", nickname = "func:plan:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Functional test plan id", required = true) @PathVariable("id") Long planId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Function plan authorized permission", required = true) @PathVariable("authPermission") FuncPlanPermission permission) {
    funcPlanAuthFacade.authCheck(planId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of functional test plan authorization", nickname = "func:plan:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<FuncPlanAuthVo>> list(@Valid FuncPlanAuthFindDto dto) {
    return ApiLocaleResult.success(funcPlanAuthFacade.list(dto));
  }

}
