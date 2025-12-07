package cloud.xcan.angus.core.tester.interfaces.script;


import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAssembler.assembleAllowImportSampleStatus;
import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAssembler.assembleInfoAllowImportSampleStatus;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.tester.script.ScriptDetailVo;
import cloud.xcan.angus.api.tester.script.dto.ScriptAddDto;
import cloud.xcan.angus.api.tester.script.dto.ScriptFindDto;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoListVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfosVo;
import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptFacade;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptImportDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Script", description = "Script Management API - Test script lifecycle management system with dependency mapping and execution controls")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptRest {

  @Resource
  private ScriptFacade scriptFacade;

  @Operation(summary = "Create script",
      description = "Create new test script with configuration and metadata",
      operationId = "script:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Script created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScriptAddDto dto) {
    return ApiLocaleResult.success(scriptFacade.add(dto));
  }

  @Operation(summary = "Update script",
      description = "Modify existing script configuration and metadata",
      operationId = "script:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script updated successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScriptUpdateDto dto) {
    scriptFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace script",
      description = "Replace script with new configuration or create new script if identifier is null",
      operationId = "script:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScriptReplaceDto dto) {
    return ApiLocaleResult.success(scriptFacade.replace(dto));
  }

  @Operation(summary = "Clone script",
      description = "Create duplicate script with new identifier while preserving original configuration",
      operationId = "script:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Script cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Script identifier for cloning", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.clone(id));
  }

  @Operation(summary = "Import script",
      description = "Import script from file or content with format validation and processing",
      operationId = "script:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Script imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> importScript(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid ScriptImportDto dto) {
    return ApiLocaleResult.success(scriptFacade.imports(dto));
  }

  @Operation(summary = "Import script examples",
      description = "Import predefined example scripts for rapid setup and demonstration",
      operationId = "script:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Script examples imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example script association", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scriptFacade.importExample(projectId));
  }

  @Operation(summary = "Delete scripts",
      description = "Remove multiple scripts and their associated configurations",
      operationId = "script:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scripts deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE)
      @Parameter(name = "ids", description = "Script identifiers for batch deletion", required = true) @RequestParam("ids") HashSet<Long> ids) {
    scriptFacade.delete(ids);
  }

  @Operation(summary = "Query script detail",
      description = "Retrieve script configuration and metadata",
      operationId = "script:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @Parameter(name = "id", description = "Script identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @Operation(summary = "Query script info",
      description = "Retrieve script information and metadata for management purposes",
      operationId = "script:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @Parameter(name = "id", description = "Script identifier for info query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(summary = "Query multiple scripts info",
      description = "Retrieve information for multiple scripts in batch for management purposes",
      operationId = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scripts info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more scripts not found")})
  //@GetMapping(value = "/innerapi/v1/script/infos") -> Conflicted with '/api/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @Parameter(name = "ids", description = "Script identifiers for batch info query", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") Set<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @Operation(summary = "Query script target info",
      description = "Retrieve script target information and dependency mapping",
      operationId = "script:target:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script target info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @GetMapping(value = "/{id}/target/info")
  public ApiLocaleResult<ScriptInfoVo> sourceTargets(
      @Parameter(name = "id", description = "Script identifier for target info query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(summary = "Query script list",
      description = "Retrieve paginated list of scripts with filtering and sorting capabilities",
      operationId = "script:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script list retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No scripts found")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScriptListVo>> list(@Valid @ParameterObject ScriptFindDto dto) {
    PageResult<ScriptListVo> result = scriptFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @Operation(summary = "Query script info list",
      description = "Retrieve paginated list of script information for management purposes",
      operationId = "script:info:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script info list retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No scripts found")})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(
      @Valid @ParameterObject ScriptFindDto dto) {
    PageResult<ScriptInfoListVo> result = scriptFacade.infoList(dto);
    return assembleInfoAllowImportSampleStatus(result);
  }

  @Operation(summary = "Export script",
      description = "Export script configuration in specified format for backup or sharing",
      operationId = "script:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script exported successfully")
  })
  @GetMapping(value = "/{id}/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "id", description = "Script identifier for export", required = true) @PathVariable("id") Long id,
      @Parameter(name = "format", description = "Export format specification", required = true) ScriptFormat format,
      HttpServletResponse response) {
    return scriptFacade.export(id, format, response);
  }

}
