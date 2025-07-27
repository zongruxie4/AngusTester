package cloud.xcan.angus.core.tester.interfaces.script;

import cloud.xcan.angus.api.tester.script.vo.AngusScriptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptFacade;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Angus Script", description = "Angus Script Management API - Standardized test script configuration and management system for workflow-compliant test execution.")
@Validated
@RestController
@RequestMapping("/api/v1/angus/script")
public class AngusScriptRest {

  @Resource
  private ScriptFacade scriptFacade;

  @Operation(summary = "Create Angus script",
      description = "Create new test script using standardized Angus model for workflow compliance.",
      operationId = "script:angus:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Angus script created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> angusAdd(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for script association") Long projectId,
      @Valid @RequestBody AngusScript dto) {
    return ApiLocaleResult.success(scriptFacade.angusAdd(projectId, dto));
  }

  @Operation(summary = "Replace Angus script",
      description = "Replace existing Angus script with new standardized configuration.",
      operationId = "script:angus:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Angus script replaced successfully")})
  @PutMapping("/{id}")
  public ApiLocaleResult<?> angusReplace(
      @Parameter(name = "id", description = "Script identifier for replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody AngusScript dto) {
    scriptFacade.angusReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query Angus script detail",
      description = "Retrieve comprehensive Angus script configuration and metadata.",
      operationId = "script:angus:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Angus script detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Angus script not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<AngusScriptDetailVo> angusDetail(
      @Parameter(name = "id", description = "Script identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.angusDetail(id));
  }
}
