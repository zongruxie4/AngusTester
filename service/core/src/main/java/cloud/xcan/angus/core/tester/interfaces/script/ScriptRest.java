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
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptSearchDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Script", description = "Unified Script Management - Centralized management hub for AngusTester test scripts, including lifecycle controls and dependency mapping.")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptRest {

  @Resource
  private ScriptFacade scriptFacade;

  @Operation(summary = "Add script", operationId = "script:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScriptAddDto dto) {
    return ApiLocaleResult.success(scriptFacade.add(dto));
  }

  @Operation(summary = "Update script", operationId = "script:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScriptUpdateDto dto) {
    scriptFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace script", operationId = "script:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScriptReplaceDto dto) {
    return ApiLocaleResult.success(scriptFacade.replace(dto));
  }

  @Operation(summary = "Clone script", operationId = "script:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.clone(id));
  }

  @Operation(summary = "Import script", operationId = "script:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ApiLocaleResult<IdKey<Long, Object>> importScript(@Valid ScriptImportDto dto) {
    return ApiLocaleResult.success(scriptFacade.imports(dto));
  }

  @Operation(summary = "Import the inner script example", operationId = "script:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scriptFacade.importExample(projectId));
  }

  @Operation(summary = "Delete scripts", operationId = "script:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE)
      @Parameter(name = "ids", description = "Script ids", required = true) @RequestParam("ids") HashSet<Long> ids) {
    scriptFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of script", operationId = "script:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @Operation(summary = "Query the info of script", operationId = "script:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(summary = "Query the info of script", operationId = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  //@GetMapping(value = "/innerapi/v1/script/infos") -> Conflicted with '/api/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @Parameter(name = "ids", description = "Script ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") Set<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @Operation(summary = "Query the targets of script", operationId = "script:target:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/target/info")
  public ApiLocaleResult<ScriptInfoVo> sourceTargets(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(summary = "Query the list of script", operationId = "script:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScriptListVo>> list(@Valid @ParameterObject ScriptFindDto dto) {
    PageResult<ScriptListVo> result = scriptFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @Operation(summary = "Query the list of script info", operationId = "script:info:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(
      @Valid @ParameterObject ScriptFindDto dto) {
    PageResult<ScriptInfoListVo> result = scriptFacade.infoList(dto);
    return assembleInfoAllowImportSampleStatus(result);
  }

  @Operation(summary = "Fulltext search the list of script", operationId = "script:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScriptListVo>> search(
      @Valid @ParameterObject ScriptSearchDto dto) {
    PageResult<ScriptListVo> result = scriptFacade.search(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @Operation(summary = "Export script", operationId = "script:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/{id}/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "format", description = "Script format, default yml", required = true) ScriptFormat format,
      HttpServletResponse response) {
    return scriptFacade.export(id, format, response);
  }

}
