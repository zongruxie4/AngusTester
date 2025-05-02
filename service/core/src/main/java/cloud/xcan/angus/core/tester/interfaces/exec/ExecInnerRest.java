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

@Tag(name = "ExecInner", description = "Internal Execution API - Service-to-service scenario/script execution and result query")
@Validated
@RestController
public class ExecInnerRest {

  @Resource
  private ExecFacade execFacade;

  @Operation(description = "Create execution by script id.", operationId = "exec:byScript:add:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")
  })
  @PostMapping(value = EXEC_ADD_BY_SCRIPT_ENDPOINT)
  public ApiLocaleResult<IdKey<Long, Object>> addByScript(@Valid @RequestBody ExecAddByScriptDto dto) {
    return ApiLocaleResult.success(execFacade.addByScript(dto));
  }

  @Operation(description = "Start execution.", operationId = "exec:start:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Start successfully")
  })
  @PostMapping(value = EXEC_START_ENDPOINT)
  public ApiLocaleResult<List<RunnerRunVo>> start(@Valid @RequestBody ExecStartDto dto) {
    return ApiLocaleResult.success(execFacade.start(dto));
  }

  @Operation(description = "Stop execution.", operationId = "exec:stop:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stop successfully")
  })
  @PostMapping(value = EXEC_STOP_ENDPOINT)
  public ApiLocaleResult<List<RunnerStopVo>> stop(@Valid @RequestBody ExecStopDto dto) {
    return ApiLocaleResult.success(execFacade.stop(dto));
  }

  @Operation(description = "Query the detail of execution.", operationId = "exec:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/{id}")
  public ApiLocaleResult<ExecDetailVo> detail(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.detail(id));
  }

  @Operation(description = "Query the basic information of execution.", operationId = "exec:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/{id}/info")
  public ApiLocaleResult<ExecInfoVo> info(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "joinSampleSummary", description = "Join sample summary flag, default true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(execFacade.info(id, joinSampleSummary));
  }

  @Operation(description = "Query the basic information of executions.", operationId = "exec:list:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/info")
  public ApiLocaleResult<List<ExecInfoVo>> listInfo(
      @Parameter(name = "ids", description = "Execution ids", required = true) @RequestParam("ids") Set<Long> ids,
      @Parameter(name = "joinSampleSummary", description = "Join sample summary flag, default true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(execFacade.listInfo(ids, joinSampleSummary));
  }

  @Operation(description = "Query the basic information of executions.", operationId = "exec:list:info:bySource")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT_PREFIX + "/info/bySource")
  public ApiLocaleResult<List<ExecInfoVo>> listInfoBySource(
      @Parameter(name = "resourceType", description = "Execution resource type", required = true) @RequestParam("resourceType") String resourceType,
      @Parameter(name = "resourceIds", description = "Execution resource ids", required = true) @RequestParam("resourceIds") Set<Long> resourceIds,
      @Parameter(name = "joinSampleSummary", description = "Join sample summary flag, default true", required = false) @RequestParam(value = "joinSampleSummary", required = false) Boolean joinSampleSummary) {
    return ApiLocaleResult.success(
        execFacade.listInfoBySource(ScriptSource.valueOf(resourceType), resourceIds,
            joinSampleSummary));
  }

}
