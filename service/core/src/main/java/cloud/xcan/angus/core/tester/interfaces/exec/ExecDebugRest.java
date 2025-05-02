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

@Tag(name = "ExecDebug", description = "Scenario & Script Debugging API - Execution debugging and result inspection interface")
@Validated
@RestController
@RequestMapping("/api/v1/exec/debug")
public class ExecDebugRest {

  @Resource
  private ExecDebugFacade execDebugFacade;

  @Operation(description = "Start execution debug.", operationId = "exec:debug:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully")
  })
  @PostMapping("/start")
  public ApiLocaleResult<ExecDebugDetailVo> start(@Valid @RequestBody ExecDebugStartDto dto) {
    return ApiLocaleResult.success(execDebugFacade.start(dto));
  }

  @Operation(description = "Start debug execution by script.", operationId = "exec:debug:script:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully")
  })
  @PostMapping(value = "/script/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByScript(
      @Valid @RequestBody ExecDebugStartByScriptDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByScript(dto));
  }

  @Operation(description = "Start debug execution by scenario.", operationId = "exec:debug:scenario:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully")
  })
  @PostMapping(value = "/scenario/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByScenario(
      @Valid @RequestBody ExecDebugStartByScenarioDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByScenario(dto));
  }

  @Operation(description = "Start debug execution by monitor.", operationId = "exec:debug:monitor:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully")
  })
  @PostMapping(value = "/monitor/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByMonitor(
      @Valid @RequestBody ExecDebugStartByMonitorDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByMonitor(dto));
  }

  @Operation(description = "Query the detail of script debug execution.", operationId = "exec:debug:script:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/script/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> scriptDetail(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.scriptDetail(id));
  }

  @Operation(description = "Query the detail of scenario debug execution.", operationId = "exec:debug:scenario:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/scenario/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> scenarioDetail(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.scenarioDetail(id));
  }

  @Operation(description = "Query the detail of monitor debug execution.", operationId = "exec:debug:monitor:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/monitor/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> monitorDetail(
      @Parameter(name = "id", description = "Monitor id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.monitorDetail(id));
  }
}
