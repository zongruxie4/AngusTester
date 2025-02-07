package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioListVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "Scenario")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioRest {

  @Resource
  private ScenarioFacade scenarioFacade;

  @ApiOperation(value = "Add scenario", nickname = "scenario:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioAddDto dto) {
    return ApiLocaleResult.success(scenarioFacade.add(dto));
  }

  @ApiOperation(value = "Update scenario", nickname = "scenario:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioUpdateDto dto) {
    scenarioFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace scenario or create if it doesn't exist", nickname = "scenario:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScenarioReplaceDto dto) {
    return ApiLocaleResult.success(scenarioFacade.replace(dto));
  }

  @ApiOperation(value = "Move the scenario to another dir", nickname = "scenario:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Moved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping(value = "/{scenarioId}/{targetProjectId}/move")
  public ApiLocaleResult<?> move(
      @PathVariable("scenarioId") @ApiParam(name = "scenarioId", value = "Scenario id", required = true) Long scenarioId,
      @PathVariable("targetProjectId") @ApiParam(name = "targetProjectId", value = "Target parent id", required = true) Long targetProjectId) {
    scenarioFacade.move(scenarioId, targetProjectId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone scenario", nickname = "scenario:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.clone(id));
  }

  @ApiOperation(value = "Import the inner scenario example", nickname = "scenario:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> exampleImport(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scenarioFacade.exampleImport(projectId));
  }

  @ApiOperation(value = "Delete scenario", nickname = "scenario:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    scenarioFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of scenario", nickname = "scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioDetailVo> detail(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of scenario", nickname = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/list")
  public ApiLocaleResult<List<ScenarioListVo>> list(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") Set<@Min(1) Long> ids) {
    return ApiLocaleResult.success(scenarioFacade.list(ids));
  }

  @ApiOperation(value = "Query the basic information list of scenario", nickname = "scenario:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioListVo>> list(@Valid ScenarioInfoFindDto dto) {
    return ApiLocaleResult.success(scenarioFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the basic information list of scenario", nickname = "scenario:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScenarioListVo>> search(@Valid ScenarioInfoSearchDto dto) {
    return ApiLocaleResult.success(scenarioFacade.search(dto));
  }

}
