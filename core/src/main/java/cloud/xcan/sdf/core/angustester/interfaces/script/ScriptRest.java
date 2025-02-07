package cloud.xcan.sdf.core.angustester.interfaces.script;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.script.ScriptDetailVo;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptAddDto;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptFindDto;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoListVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfosVo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptFormat;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.ScriptFacade;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;
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

@Api(tags = "Script")
@Validated
@RestController
@RequestMapping("/api/v1/script")
public class ScriptRest {

  @Resource
  private ScriptFacade scriptFacade;

  @ApiOperation(value = "Add script", nickname = "script:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScriptAddDto dto) {
    return ApiLocaleResult.success(scriptFacade.add(dto));
  }

  @ApiOperation(value = "Update script", nickname = "script:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScriptUpdateDto dto) {
    scriptFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace script", nickname = "script:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ScriptReplaceDto dto) {
    return ApiLocaleResult.success(scriptFacade.replace(dto));
  }

  @ApiOperation(value = "Delete scripts", nickname = "script:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @ApiParam(name = "ids", value = "Script ids", required = true) @RequestParam("ids") HashSet<Long> ids) {
    scriptFacade.delete(ids);
  }

  @ApiOperation(value = "Clone script", nickname = "script:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.clone(id));
  }

  @ApiOperation(value = "Import the inner script example", nickname = "script:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> exampleImport(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(scriptFacade.exampleImport(projectId));
  }

  @ApiOperation(value = "Import script", nickname = "script:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ApiLocaleResult<IdKey<Long, Object>> importScript(@Valid ScriptImportDto dto) {
    return ApiLocaleResult.success(scriptFacade.imports(dto));
  }

  @ApiOperation(value = "Export script", nickname = "script:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/{id}/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "format", value = "Script format, default yml", required = true) ScriptFormat format,
      HttpServletResponse response) {
    return scriptFacade.export(id, format, response);
  }

  @ApiOperation(value = "Query the detail of script", nickname = "script:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @ApiOperation(value = "Query the info of script", nickname = "script:info")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @ApiOperation(value = "Query the info of script", nickname = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  //@GetMapping(value = "/doorapi/v1/script/infos") -> Conflicted with '/api/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @ApiParam(name = "ids", value = "Script ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") Set<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @ApiOperation(value = "Query the targets of script", nickname = "script:target:info")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/target/info")
  public ApiLocaleResult<ScriptInfoVo> sourceTargets(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @ApiOperation(value = "Query the list of script", nickname = "script:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ScriptListVo>> list(@Valid ScriptFindDto dto) {
    PageResult<ScriptListVo> result = scriptFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @ApiOperation(value = "Query the list of script info", nickname = "script:info:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(@Valid ScriptFindDto dto) {
    PageResult<ScriptInfoListVo> result = scriptFacade.infoList(dto);
    return assembleInfoAllowImportSampleStatus(result);
  }

  @ApiOperation(value = "Fulltext search the list of script", nickname = "script:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScriptListVo>> search(@Valid ScriptSearchDto dto) {
    PageResult<ScriptListVo> result = scriptFacade.search(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @NotNull
  private ApiLocaleResult<PageResult<ScriptListVo>> assembleAllowImportSampleStatus(
      PageResult<ScriptListVo> result) {
    ApiLocaleResult<PageResult<ScriptListVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }

  @NotNull
  private ApiLocaleResult<PageResult<ScriptInfoListVo>> assembleInfoAllowImportSampleStatus(
      PageResult<ScriptInfoListVo> result) {
    ApiLocaleResult<PageResult<ScriptInfoListVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }
}
