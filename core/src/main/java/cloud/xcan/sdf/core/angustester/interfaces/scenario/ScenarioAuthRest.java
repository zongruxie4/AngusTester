package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
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

@Api(tags = "ScenarioAuth")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioAuthRest {

  @Resource
  private ScenarioAuthFacade scenarioAuthFacade;

  @ApiOperation(value = "Add the authorization of scenario", nickname = "scenario:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @RequestBody ScenarioAuthAddDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.add(scenarioId, dto));
  }

  @ApiOperation(value = "Replace the authorization of scenario", nickname = "scenario:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Scenario authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScenarioAuthReplaceDto dto) {
    scenarioAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of scenario", nickname = "scenario:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioAuthFacade.enabled(scenarioId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of scenario", nickname = "scenario:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioAuthFacade.status(scenarioId));
  }

  @ApiOperation(value = "Delete the authorization of scenario", nickname = "scenario:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Scenario authorization id", required = true) @PathVariable("id") Long id) {
    scenarioAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of scenario and throw 404 when scenario doesn't exist", nickname = "scenario:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScenarioPermission>> userAuth(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(scenarioAuthFacade.userAuth(scenarioId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of scenario and throw 404 when scenario doesn't exist", nickname = "scenario:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScenarioAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(scenarioAuthFacade.currentUserAuth(scenarioId, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of scenario, the administrator permission is included", nickname = "scenario:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Scenario authorized permission", required = true) @PathVariable("authPermission") ScenarioPermission permission) {
    scenarioAuthFacade.authCheck(scenarioId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of scenario authorization", nickname = "scenario:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScenarioAuthVo>> list(@Valid ScenarioAuthFindDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.list(dto));
  }

}
