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
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecSearchDto;
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

@Tag(name = "Execution", description = "User Execution API - End-user scenario/script execution and result access")
@Validated
@RestController
@RequestMapping("/api/v1/exec")
public class ExecRest {

  @Resource
  private ExecFacade execFacade;

  @Operation(summary = "Create execution by script content", operationId = "exec:byContent:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")
  })
  @PostMapping(value = "/bycontent")
  public ApiLocaleResult<IdKey<Long, Object>> addByScriptContent(
      @Valid @RequestBody ExecAddByContentDto dto) {
    return ApiLocaleResult.success(execFacade.addByScriptContent(dto));
  }

  @Operation(summary = "Create execution by script arguments", operationId = "exec:byArgs:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")
  })
  @PostMapping(value = "/byargs")
  public ApiLocaleResult<IdKey<Long, Object>> addByScriptArgs(
      @Valid @RequestBody ExecAddByArgsDto dto) {
    return ApiLocaleResult.success(execFacade.addByScriptArgs(dto));
  }

  @Operation(summary = "Create execution by script id", operationId = "exec:byScript:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")
  })
  @PostMapping(value = "/byscript")
  public ApiLocaleResult<IdKey<Long, Object>> addByScript(
      @Valid @RequestBody ExecAddByScriptDto dto) {
    return ApiLocaleResult.success(execFacade.addByScript(dto));
  }

  @Operation(summary = "Replace execution configuration", operationId = "exec:config:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modified successfully")
  })
  @PutMapping(value = "/{id}/config")
  public ApiLocaleResult<?> configReplace(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ExecConfigReplaceDto dto) {
    execFacade.configReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace execution script configuration", operationId = "exec:script:config:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modified successfully")
  })
  @PutMapping(value = "/{id}/script/config")
  public ApiLocaleResult<?> scriptConfigReplace(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ExecScriptConfigReplaceDto dto) {
    execFacade.scriptConfigReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Start execution", operationId = "exec:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Start successfully")
  })
  @PostMapping(value = "/start")
  public ApiLocaleResult<List<RunnerRunVo>> start(@Valid @RequestBody ExecStartDto dto) {
    return ApiLocaleResult.success(execFacade.start(dto));
  }

  @Operation(summary = "Stop execution", operationId = "exec:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stop successfully")
  })
  @PostMapping(value = "/stop")
  public ApiLocaleResult<List<RunnerStopVo>> stop(@Valid @RequestBody ExecStopDto dto) {
    return ApiLocaleResult.success(execFacade.stop(dto));
  }

  @Operation(summary = "Delete executions", operationId = "exec:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> ids) {
    execFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of execution", operationId = "exec:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ExecDetailVo> detail(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.detail(id));
  }

  @Operation(summary = "Query the basic information of execution", operationId = "exec:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ExecInfoVo> info(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.info(id, true));
  }

  @Operation(summary = "Query the actual script of execution", operationId = "exec:script")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/script")
  public ApiLocaleResult<String> script(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(execFacade.script(id));
  }

  @Operation(summary = "Query all testing server configurations of the execution, only Http plugin is supported", operationId = "exec:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @GetMapping("/{id}/test/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execFacade.serverList(id));
  }

  @Operation(summary = "Query the list of execution", operationId = "exec:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ExecVo>> list(@Valid @ParameterObject ExecFindDto dto) {
    return ApiLocaleResult.success(execFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of execution", operationId = "exec:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ExecVo>> search(@Valid @ParameterObject ExecSearchDto dto) {
    return ApiLocaleResult.success(execFacade.search(dto));
  }

}
