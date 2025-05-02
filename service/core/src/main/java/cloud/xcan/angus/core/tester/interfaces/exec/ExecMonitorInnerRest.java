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

@Tag(name = "ExecDebugInner", description = "Execution Scenario Monitoring (Internal) API - Internal service integration for scenario execution monitoring")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec/debug")
public class ExecMonitorInnerRest {

  @Resource
  private ExecDebugFacade execDebugFacade;

  @Operation(description = "Start debug execution by monitor.", operationId = "exec:debug:monitor:start:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started successfully")
  })
  @PostMapping(value = "/monitor/start")
  public ApiLocaleResult<ExecDebugDetailVo> startByMonitor(
      @Valid @RequestBody ExecDebugStartByMonitorDto dto) {
    return ApiLocaleResult.success(execDebugFacade.startByMonitor(dto));
  }

  @Operation(description = "Query the detail of monitor debug execution.", operationId = "exec:debug:monitor:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/monitor/{id}")
  public ApiLocaleResult<ExecDebugDetailVo> monitorDetail(
      @Parameter(name = "id", description = "Monitor id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execDebugFacade.monitorDetail(id));
  }
}
