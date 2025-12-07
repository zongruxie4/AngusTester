package cloud.xcan.angus.core.tester.interfaces.exec;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByArgsDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByContentDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecScriptConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecInfoVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Execution", description = "Test Execution Management - APIs for scenario and script execution management including execution creation, configuration, monitoring, and result access with real-time control capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/exec")
public class ExecRest {

  @Resource
  private ExecFacade execFacade;

  @Operation(summary = "Create execution by script content",
      description = "Create a new test execution using script content with configuration options",
      operationId = "exec:byContent:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution created successfully by script content")
  })
  @PostMapping(value = "/bycontent")
  public ApiLocaleResult<IdKey<Long, Object>> addByScriptContent(
      @Valid @RequestBody ExecAddByContentDto dto) {
    return ApiLocaleResult.success(execFacade.addByScriptContent(dto));
  }

  @Operation(summary = "Create execution by script arguments",
      description = "Create a new test execution using script arguments with dynamic parameter configuration",
      operationId = "exec:byArgs:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution created successfully by script arguments")
  })
  @PostMapping(value = "/byargs")
  public ApiLocaleResult<IdKey<Long, Object>> addByScriptArgs(
      @Valid @RequestBody ExecAddByArgsDto dto) {
    return ApiLocaleResult.success(execFacade.addByScriptArgs(dto));
  }

  @Operation(summary = "Create execution by script identifier",
      description = "Create a new test execution using existing script identifier with reference configuration",
      operationId = "exec:byScript:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution created successfully by script identifier")
  })
  @PostMapping(value = "/byscript")
  public ApiLocaleResult<IdKey<Long, Object>> addByScript(
      @Valid @RequestBody ExecAddByScriptDto dto) {
    return ApiLocaleResult.success(execFacade.addByScript(dto));
  }

  @Operation(summary = "Replace execution configuration",
      description = "Update execution configuration with parameter and setting modifications",
      operationId = "exec:config:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution configuration replaced successfully")
  })
  @PutMapping(value = "/{id}/config")
  public ApiLocaleResult<?> configReplace(
      @Parameter(name = "id", description = "Execution identifier for configuration replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ExecConfigReplaceDto dto) {
    execFacade.configReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace execution script configuration",
      description = "Update execution script configuration with script parameter modifications",
      operationId = "exec:script:config:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution script configuration replaced successfully")
  })
  @PutMapping(value = "/{id}/script/config")
  public ApiLocaleResult<?> scriptConfigReplace(
      @Parameter(name = "id", description = "Execution identifier for script configuration replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ExecScriptConfigReplaceDto dto) {
    execFacade.scriptConfigReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Start execution",
      description = "Initiate test execution with runner management and real-time monitoring",
      operationId = "exec:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution started successfully")
  })
  @PostMapping(value = "/start")
  public ApiLocaleResult<List<RunnerRunVo>> start(@Valid @RequestBody ExecStartDto dto) {
    return ApiLocaleResult.success(execFacade.start(dto));
  }

  @Operation(summary = "Stop execution",
      description = "Terminate test execution with graceful shutdown and cleanup procedures",
      operationId = "exec:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution stopped successfully")
  })
  @PostMapping(value = "/stop")
  public ApiLocaleResult<List<RunnerStopVo>> stop(@Valid @RequestBody ExecStopDto dto) {
    return ApiLocaleResult.success(execFacade.stop(dto));
  }

  @Operation(summary = "Delete executions",
      description = "Remove multiple executions from the system with batch operation support",
      operationId = "exec:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Executions deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> ids) {
    execFacade.delete(ids);
  }

  @Operation(summary = "Get execution details",
      description = "Retrieve execution details including configuration, status, and metadata",
      operationId = "exec:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Execution not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ExecDetailVo> detail(
      @Parameter(name = "id", description = "Execution identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.detail(id));
  }

  @Operation(summary = "Get execution basic information",
      description = "Retrieve basic execution information including status, metadata, and summary details",
      operationId = "exec:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution basic information retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Execution not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ExecInfoVo> info(
      @Parameter(name = "id", description = "Execution identifier for basic information retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.info(id, true));
  }

  @Operation(summary = "Get execution script content",
      description = "Retrieve the actual script content used for execution with formatting and syntax highlighting",
      operationId = "exec:script")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution script content retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Execution not found")})
  @GetMapping(value = "/{id}/script")
  public ApiLocaleResult<String> script(
      @Parameter(name = "id", description = "Execution identifier for script content retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(execFacade.script(id));
  }

  @Operation(summary = "Get execution server configurations",
      description = "Retrieve all testing server configurations for the execution with HTTP plugin support",
      operationId = "exec:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution server configurations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Server configurations not found")
  })
  @GetMapping("/{id}/test/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Execution identifier for server configuration retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.serverList(id));
  }

  @Operation(summary = "Query execution list",
      description = "Retrieve paginated list of executions with filtering and search options",
      operationId = "exec:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ExecVo>> list(@Valid @ParameterObject ExecFindDto dto) {
    return ApiLocaleResult.success(execFacade.list(dto));
  }

}
