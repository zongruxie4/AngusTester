package cloud.xcan.angus.core.tester.interfaces.scenario;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.tester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.angus.api.tester.scenario.vo.ScenarioListVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioInfoSearchDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
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

@Tag(name = "Scenario", description = "Scenario Design and Orchestration Management - Visually design and manage complex test workflows through a UI-driven interface.")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioRest {

  @Resource
  private ScenarioFacade scenarioFacade;

  @Operation(description = "Add scenario", operationId = "scenario:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioAddDto dto) {
    return ApiLocaleResult.success(scenarioFacade.add(dto));
  }

  @Operation(description = "Update scenario", operationId = "scenario:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioUpdateDto dto) {
    scenarioFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace scenario or create if it doesn't exist", operationId = "scenario:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScenarioReplaceDto dto) {
    return ApiLocaleResult.success(scenarioFacade.replace(dto));
  }

  @Operation(description = "Move the scenario to another dir", operationId = "scenario:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Moved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping(value = "/{scenarioId}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @PathVariable("scenarioId") @Parameter(name = "scenarioId", description = "Scenario id", required = true) Long scenarioId,
      @PathVariable("targetProjectId") @Parameter(name = "targetProjectId", description = "Target parent id", required = true) Long targetProjectId) {
    scenarioFacade.move(scenarioId, targetProjectId);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Clone scenario", operationId = "scenario:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.clone(id));
  }

  @Operation(description = "Import the inner scenario example", operationId = "scenario:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scenarioFacade.importExample(projectId));
  }

  @Operation(description = "Delete scenario", operationId = "scenario:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    scenarioFacade.delete(id);
  }

  @Operation(description = "Query the detail of scenario", operationId = "scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioDetailVo> detail(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.detail(id));
  }

  @Operation(description = "Query the list of scenario", operationId = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/list")
  public ApiLocaleResult<List<ScenarioListVo>> list(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") Set<@Min(1) Long> ids) {
    return ApiLocaleResult.success(scenarioFacade.list(ids));
  }

  @Operation(description = "Query the basic information list of scenario", operationId = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioListVo>> list(@Valid ScenarioInfoFindDto dto) {
    return ApiLocaleResult.success(scenarioFacade.list(dto));
  }

  @Operation(description = "Fulltext search the basic information list of scenario", operationId = "scenario:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScenarioListVo>> search(@Valid ScenarioInfoSearchDto dto) {
    return ApiLocaleResult.success(scenarioFacade.search(dto));
  }

}
