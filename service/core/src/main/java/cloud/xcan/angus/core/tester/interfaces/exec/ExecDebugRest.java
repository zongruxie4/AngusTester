package cloud.xcan.angus.core.tester.interfaces.exec;


import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecDebugFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByMonitorDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScenarioDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Execution Debug", description = "Scenario & Script Debugging Management - Comprehensive APIs for execution debugging, result inspection, and step-by-step analysis with real-time debugging capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/exec/debug")
public class ExecDebugRest {

  @Resource
  private ExecDebugFacade execDebugFacade;

  @Operation(summary = "Start execution debug session", 
      description = "Initiate a new execution debug session with comprehensive debugging configuration and real-time monitoring",
      operationId = "exec:debug:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution debug session started successfully")
  })
  @PostMapping("/start")
  public ApiLocaleResult<ExecDebugDetailVo> start(@Valid @RequestBody ExecDebugStartDto dto) {
    return ApiLocaleResult.success(execDebugFacade.start(dto));
  }

  @Operation(summary = "Start debug execution by script", 
      description = "Initiate script-based debug execution with comprehensive script analysis and debugging capabilities",
      operationId = "exec:debug:script:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script debug execution started successfully")
  })
  @PostMapping(value = "/script/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByScript(
      @Valid @RequestBody ExecDebugStartByScriptDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByScript(dto));
  }

  @Operation(summary = "Start debug execution by scenario", 
      description = "Initiate scenario-based debug execution with comprehensive scenario analysis and debugging capabilities",
      operationId = "exec:debug:scenario:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario debug execution started successfully")
  })
  @PostMapping(value = "/scenario/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByScenario(
      @Valid @RequestBody ExecDebugStartByScenarioDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByScenario(dto));
  }

  @Operation(summary = "Start debug execution by monitor", 
      description = "Initiate monitor-based debug execution with comprehensive monitoring analysis and debugging capabilities",
      operationId = "exec:debug:monitor:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor debug execution started successfully")
  })
  @PostMapping(value = "/monitor/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByMonitor(
      @Valid @RequestBody ExecDebugStartByMonitorDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByMonitor(dto));
  }

  @Operation(summary = "Get script debug execution details", 
      description = "Retrieve comprehensive script debug execution details including step-by-step analysis and debugging information",
      operationId = "exec:debug:script:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script debug execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script debug execution not found")})
  @GetMapping(value = "/script/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> scriptDetail(
      @Parameter(name = "id", description = "Script debug execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.scriptDetail(id));
  }

  @Operation(summary = "Get scenario debug execution details", 
      description = "Retrieve comprehensive scenario debug execution details including step-by-step analysis and debugging information",
      operationId = "exec:debug:scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario debug execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario debug execution not found")})
  @GetMapping(value = "/scenario/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> scenarioDetail(
      @Parameter(name = "id", description = "Scenario debug execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.scenarioDetail(id));
  }

  @Operation(summary = "Get monitor debug execution details", 
      description = "Retrieve comprehensive monitor debug execution details including step-by-step analysis and debugging information",
      operationId = "exec:debug:monitor:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor debug execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Monitor debug execution not found")})
  @GetMapping(value = "/monitor/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> monitorDetail(
      @Parameter(name = "id", description = "Monitor debug execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.monitorDetail(id));
  }
}
