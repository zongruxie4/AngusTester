package cloud.xcan.angus.core.tester.interfaces.exec;


import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecDebugFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByMonitorDto;
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

@Tag(name = "Execution Monitor - Internal", description = "Internal Execution Monitor Management - Internal service integration for scenario execution monitoring with monitoring capabilities")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec/debug")
public class ExecMonitorInnerRest {

  @Resource
  private ExecDebugFacade execDebugFacade;

  @Operation(summary = "Start monitor debug execution",
      description = "Initiate monitor-based debug execution with internal service integration and comprehensive monitoring analysis",
      operationId = "exec:debug:monitor:start:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor debug execution started successfully")
  })
  @PostMapping(value = "/monitor/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByMonitor(
      @Valid @RequestBody ExecDebugStartByMonitorDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByMonitor(dto));
  }

  @Operation(summary = "Get monitor debug execution details",
      description = "Retrieve comprehensive monitor debug execution details for internal service integration",
      operationId = "exec:debug:monitor:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor debug execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Monitor debug execution not found")})
  @GetMapping(value = "/monitor/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> monitorDetail(
      @Parameter(name = "id", description = "Monitor debug execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.monitorDetail(id));
  }
}
