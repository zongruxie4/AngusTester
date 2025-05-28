package cloud.xcan.angus.core.tester.interfaces.scenario;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorSearchDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorListVo;
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
import jakarta.validation.constraints.Size;
import java.util.HashSet;
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

@Tag(name = "ScenarioMonitor", description = "Scenario Monitoring Config - Configure and manage monitoring rules to track system health at the API level, enabling proactive issue detection and remediation.")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/monitor")
public class ScenarioMonitorRest {

  @Resource
  private ScenarioMonitorFacade scenarioMonitorFacade;

  @Operation(summary = "Create scenario monitor", operationId = " scenario:monitor:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioMonitorAddDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.add(dto));
  }

  @Operation(summary = "Update scenario monitor", operationId = " scenario:monitor:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioMonitorUpdateDto dto) {
    scenarioMonitorFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace scenario monitor", operationId = " scenario:monitor:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScenarioMonitorReplaceDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.replace(dto));
  }

  @Operation(summary = "Run scenario monitoring immediately", operationId = " scenario:monitor:run:now")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Generated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PostMapping("/{id}/run")
  public ApiLocaleResult<?> runNow(
      @Parameter(name = "id", description = "Scenario monitor id", required = true) @PathVariable("id") Long id) {
    scenarioMonitorFacade.runNow(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete scenario monitor", operationId = " scenario:monitor:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Scenario monitor ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    scenarioMonitorFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of scenario monitor", operationId = " scenario:monitor:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorDetailVo> detail(
      @Parameter(name = "id", description = "Scenario monitor id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorFacade.detail(id));
  }

  @Operation(summary = "Query the list of scenario monitor", operationId = " scenario:monitor:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorListVo>> list(
      @Valid @ParameterObject ScenarioMonitorFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of scenario monitor", operationId = " scenario:monitor:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScenarioMonitorListVo>> search(
      @Valid @ParameterObject ScenarioMonitorSearchDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.search(dto));
  }

}
