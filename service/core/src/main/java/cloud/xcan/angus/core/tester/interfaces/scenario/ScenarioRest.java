package cloud.xcan.angus.core.tester.interfaces.scenario;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
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

@Tag(name = "Scenario", description = "Scenario Design and Management API - Visual workflow design and orchestration system for creating, managing, and executing complex test scenarios through UI-driven interfaces")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioRest {

  @Resource
  private ScenarioFacade scenarioFacade;

  @Operation(summary = "Create scenario",
      description = "Create new test scenario with visual workflow design and configuration",
      operationId = "scenario:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioAddDto dto) {
    return ApiLocaleResult.success(scenarioFacade.add(dto));
  }

  @Operation(summary = "Update scenario",
      description = "Modify existing scenario configuration, workflow design, and properties",
      operationId = "scenario:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioUpdateDto dto) {
    scenarioFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace scenario",
      description = "Replace scenario with new configuration or create new scenario if identifier is null",
      operationId = "scenario:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScenarioReplaceDto dto) {
    return ApiLocaleResult.success(scenarioFacade.replace(dto));
  }

  @Operation(summary = "Move scenario to another project",
      description = "Transfer scenario ownership and location to a different project",
      operationId = "scenario:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario moved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario or target project not found")})
  @PatchMapping(value = "/{scenarioId}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @PathVariable("scenarioId") @Parameter(name = "scenarioId", description = "Scenario identifier for transfer", required = true) Long scenarioId,
      @PathVariable("targetProjectId") @Parameter(name = "targetProjectId", description = "Target project identifier for scenario transfer", required = true) Long targetProjectId) {
    scenarioFacade.move(scenarioId, targetProjectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone scenario",
      description = "Create a duplicate copy of existing scenario with all configuration and workflow design",
      operationId = "scenario:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Scenario identifier for cloning", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.clone(id));
  }

  @Operation(summary = "Import scenario examples",
      description = "Import predefined example scenarios for rapid setup and demonstration purposes",
      operationId = "scenario:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario examples imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scenarioFacade.importExample(projectId));
  }

  @Operation(summary = "Delete scenario",
      description = "Permanently delete scenario and move to recycle bin for potential recovery",
      operationId = "scenario:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Scenario identifier for deletion", required = true) @PathVariable("id") Long id) {
    scenarioFacade.delete(id);
  }

  @Operation(summary = "Query scenario detail",
      description = "Retrieve scenario information including workflow design and configuration",
      operationId = "scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioDetailVo> detail(
      @Parameter(name = "id", description = "Scenario identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.detail(id));
  }

  @Operation(summary = "Query scenarios by identifiers",
      description = "Retrieve basic information for multiple scenarios specified by their identifiers",
      operationId = "scenario:list:byIds")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenarios retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more scenarios not found")})
  @GetMapping(value = "/list")
  public ApiLocaleResult<List<ScenarioListVo>> list(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") Set<@Min(1) Long> ids) {
    return ApiLocaleResult.success(scenarioFacade.list(ids));
  }

  @Operation(summary = "Query scenario list",
      description = "Retrieve paginated list of scenarios with filtering and search capabilities",
      operationId = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioListVo>> list(
      @Valid @ParameterObject ScenarioInfoFindDto dto) {
    return ApiLocaleResult.success(scenarioFacade.list(dto));
  }

}
