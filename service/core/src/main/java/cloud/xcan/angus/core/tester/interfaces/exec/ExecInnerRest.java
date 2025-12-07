package cloud.xcan.angus.core.tester.interfaces.exec;


import static cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd.EXEC_ADD_BY_SCRIPT_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd.EXEC_ENDPOINT_PREFIX;
import static cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd.EXEC_START_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd.EXEC_STOP_ENDPOINT;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecInfoVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Execution - Internal", description = "Internal Execution Management - Service-to-service scenario/script execution and result query with internal integration capabilities")
@Validated
@RestController
public class ExecInnerRest {

  @Resource
  private ExecFacade execFacade;

  @Operation(summary = "Create execution by script identifier",
      description = "Create a new test execution using existing script identifier with internal service integration",
      operationId = "exec:byScript:add:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution created successfully by script identifier")
  })
  @PostMapping(value = EXEC_ADD_BY_SCRIPT_ENDPOINT)
  public ApiLocaleResult<IdKey<Long, Object>> addByScript(
      @Valid @RequestBody ExecAddByScriptDto dto) {
    return ApiLocaleResult.success(execFacade.addByScript(dto));
  }

  @Operation(summary = "Start execution",
      description = "Initiate test execution with internal service integration and runner management",
      operationId = "exec:start:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution started successfully")
  })
  @PostMapping(value = EXEC_START_ENDPOINT)
  public ApiLocaleResult<List<RunnerRunVo>> start(@Valid @RequestBody ExecStartDto dto) {
    return ApiLocaleResult.success(execFacade.start(dto));
  }

  @Operation(summary = "Stop execution",
      description = "Terminate test execution with graceful shutdown and internal service cleanup",
      operationId = "exec:stop:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution stopped successfully")
  })
  @PostMapping(value = EXEC_STOP_ENDPOINT)
  public ApiLocaleResult<List<RunnerStopVo>> stop(@Valid @RequestBody ExecStopDto dto) {
    return ApiLocaleResult.success(execFacade.stop(dto));
  }

  @Operation(summary = "Get execution details",
      description = "Retrieve comprehensive execution details for internal service integration",
      operationId = "exec:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Execution not found")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/{id}")
  public ApiLocaleResult<ExecDetailVo> detail(
      @Parameter(name = "id", description = "Execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.detail(id));
  }

  @Operation(summary = "Get execution basic information",
      description = "Retrieve basic execution information with optional sample summary integration",
      operationId = "exec:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution basic information retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/{id}/info")
  public ApiLocaleResult<ExecInfoVo> info(
      @Parameter(name = "id", description = "Execution identifier for basic information retrieval", required = true) @PathVariable("id") Long id,
      @Parameter(name = "joinSampleSummary", description = "Sample summary integration flag for comprehensive data retrieval, defaults to true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(execFacade.info(id, joinSampleSummary));
  }

  @Operation(summary = "Get multiple executions basic information",
      description = "Retrieve basic information for multiple executions with optional sample summary integration",
      operationId = "exec:list:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Multiple executions basic information retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/info")
  public ApiLocaleResult<List<ExecInfoVo>> listInfo(
      @Parameter(name = "ids", description = "Execution identifiers for batch information retrieval", required = true) @RequestParam("ids") Set<Long> ids,
      @Parameter(name = "joinSampleSummary", description = "Sample summary integration flag for comprehensive data retrieval, defaults to true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(execFacade.listInfo(ids, joinSampleSummary));
  }

  @Operation(summary = "Get executions basic information by source",
      description = "Retrieve basic information for executions filtered by resource type and identifiers",
      operationId = "exec:list:info:bySource")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Executions basic information retrieved successfully by source")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/info/bySource")
  public ApiLocaleResult<List<ExecInfoVo>> listInfoBySource(
      @Parameter(name = "resourceType", description = "Execution resource type for source-based filtering", required = true) @RequestParam("resourceType") String resourceType,
      @Parameter(name = "resourceIds", description = "Execution resource identifiers for source-based filtering", required = true) @RequestParam("resourceIds") Set<Long> resourceIds,
      @Parameter(name = "joinSampleSummary", description = "Sample summary integration flag for comprehensive data retrieval, defaults to true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(
        execFacade.listInfoBySource(ScriptSource.valueOf(resourceType), resourceIds,
            joinSampleSummary));
  }

}
