package cloud.xcan.angus.core.tester.interfaces.scenario;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
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

@Tag(name = "Scenario Monitor", description = "Scenario Monitoring Management API - Comprehensive monitoring configuration and management system for proactive issue detection and remediation at the API level.")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/monitor")
public class ScenarioMonitorRest {

  @Resource
  private ScenarioMonitorFacade scenarioMonitorFacade;

  @Operation(summary = "Create scenario monitor",
      description = "Create new monitoring configuration for proactive issue detection and alerting.",
      operationId = "scenario:monitor:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario monitor created successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioMonitorAddDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.add(dto));
  }

  @Operation(summary = "Update scenario monitor",
      description = "Modify existing monitoring configuration and alert settings.",
      operationId = "scenario:monitor:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario monitor updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario monitor not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioMonitorUpdateDto dto) {
    scenarioMonitorFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace scenario monitor",
      description = "Replace monitoring configuration with new settings or create new monitor if identifier is null.",
      operationId = "scenario:monitor:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario monitor replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario monitor not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScenarioMonitorReplaceDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.replace(dto));
  }

  @Operation(summary = "Execute scenario monitoring immediately",
      description = "Trigger immediate monitoring execution for real-time health check and alert generation.",
      operationId = "scenario:monitor:run:now")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitoring execution triggered successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario monitor not found")
  })
  @PostMapping("/{id}/run")
  public ApiLocaleResult<?> runNow(
      @Parameter(name = "id", description = "Scenario monitor identifier for immediate execution", required = true) @PathVariable("id") Long id) {
    scenarioMonitorFacade.runNow(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete scenario monitors",
      description = "Remove monitoring configurations and stop associated alerting for specified monitors.",
      operationId = "scenario:monitor:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario monitors deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Scenario monitor identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    scenarioMonitorFacade.delete(ids);
  }

  @Operation(summary = "Query scenario monitor detail",
      description = "Retrieve comprehensive monitoring configuration and current status.",
      operationId = "scenario:monitor:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario monitor detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario monitor not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorDetailVo> detail(
      @Parameter(name = "id", description = "Scenario monitor identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorFacade.detail(id));
  }

  @Operation(summary = "Query scenario monitor list",
      description = "Retrieve paginated list of monitoring configurations with filtering capabilities.",
      operationId = "scenario:monitor:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario monitor list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorListVo>> list(
      @Valid @ParameterObject ScenarioMonitorFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.list(dto));
  }

}
