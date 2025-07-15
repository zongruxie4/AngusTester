package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
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

@Tag(name = "ScenarioAuth", description = "Scenario Authorization Management - Unified entry for managing data access permissions of test scenario")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioAuthRest {

  @Resource
  private ScenarioAuthFacade scenarioAuthFacade;

  @Operation(summary = "Add the authorization of scenario", operationId = "scenario:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @RequestBody ScenarioAuthAddDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.add(scenarioId, dto));
  }

  @Operation(summary = "Replace the authorization of scenario", operationId = "scenario:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Scenario authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ScenarioAuthReplaceDto dto) {
    scenarioAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable the authorization of scenario", operationId = "scenario:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled or Disabled", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    scenarioAuthFacade.enabled(scenarioId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query authorization status of scenario", operationId = "scenario:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId) {
    return ApiLocaleResult.success(scenarioAuthFacade.status(scenarioId));
  }

  @Operation(summary = "Delete the authorization of scenario", operationId = "scenario:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Scenario authorization id", required = true) @PathVariable("id") Long id) {
    scenarioAuthFacade.delete(id);
  }

  @Operation(summary = "Query the user authorization permission of scenario", operationId = "scenario:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ScenarioPermission>> userAuth(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "userId", description = "userId", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(scenarioAuthFacade.userAuth(scenarioId, userId, admin));
  }

  @Operation(summary = "Query the current user authorization permissions of scenario", operationId = "scenario:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ScenarioAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "admin", description = "Required when the query contains administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(scenarioAuthFacade.currentUserAuth(scenarioId, admin));
  }

  @Operation(summary = "Check the user authorization or administrator permission of scenario", operationId = "scenario:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long scenarioId,
      @Parameter(name = "userId", description = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Scenario authorized permission", required = true)
      @PathVariable("authPermission") ScenarioPermission permission) {
    scenarioAuthFacade.authCheck(scenarioId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of scenario authorization", operationId = "scenario:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ScenarioAuthVo>> list(
      @Valid @ParameterObject ScenarioAuthFindDto dto) {
    return ApiLocaleResult.success(scenarioAuthFacade.list(dto));
  }

}
