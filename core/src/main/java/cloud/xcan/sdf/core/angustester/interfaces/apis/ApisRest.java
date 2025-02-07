package cloud.xcan.sdf.core.angustester.interfaces.apis;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisMoveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.mock.ApisAssocMockApisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
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

@Api(tags = "Apis")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisRest {

  @Resource
  private ApisFacade apisFacade;

  @ApiOperation(value = "Archive the unarchived apis", nickname = "apis:archive")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful save", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/archive")
  public ApiLocaleResult<List<IdKey<Long, Object>>> archive(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisArchiveDto> dtos) {
    return ApiLocaleResult.success(apisFacade.archive(dtos));
  }

  @ApiOperation(value = "Update apis", nickname = "apis:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisUpdateDto> dto) {
    apisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace apis", nickname = "apis:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisReplaceDto> dto) {
    return ApiLocaleResult.success(apisFacade.replace(dto));
  }

  @ApiOperation(value = "Update the name of apis", nickname = "apis:replace:name")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = DEFAULT_NAME_LENGTH_X2) @ApiParam(name = "name", value = "New apis name", required = true) @RequestParam("name") String name) {
    apisFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Move the apis to another services", nickname = "apis:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Move successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/move")
  public ApiLocaleResult<?> move(@Valid @RequestBody ApisMoveDto dto) {
    apisFacade.move(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone apis", nickname = "apis:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.clone(id));
  }

  @ApiOperation(value = "Delete apis", nickname = "apis:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Apis ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisFacade.delete(ids);
  }

  @ApiOperation(value = "Modify apis status", nickname = "apis:status:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Modified successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "status", value = "Apis status", required = true) @RequestParam("status") ApiStatus status) {
    apisFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace server configuration of the apis", nickname = "apis:schema:server:replace",
      notes = "A declaration of which servers can be used across the API. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/schema/server")
  public ApiLocaleResult<?> serverReplace(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody Server dto) {
    apisFacade.serverReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace all server configuration of the apis", nickname = "apis:schema:server:all:replace",
      notes = "A declaration of which servers can be used across the API. Note: `The local server will be deleted when it does not exist in the request`. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Api not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/schema/server/all")
  public ApiLocaleResult<?> serverReplaceAll(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<Server> dtos) {
    apisFacade.serverReplaceAll(id, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete servers of the apis", nickname = "apis:schema:server:delete",
      notes = "Delete the servers of apis by url")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/schema/server")
  public void serverDelete(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "urls", value = "Server url", required = true) @RequestParam("urls") Set<String> urls) {
    apisFacade.serverDelete(id, urls);
  }

  @ApiOperation(value = "Query the server configuration of apis", nickname = "apis:schema:server:all",
      notes = "Note: `The data source includes the current api request server, api servers configuration, and parent services servers configuration`.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Api not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{id}/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.serverList(id));
  }

  @ApiOperation(value = "Add mock apis association", nickname = "apis:association:mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<IdKey<Long, Object>> assocMockApisAdd(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAssocMockApisAddDto dto) {
    return ApiLocaleResult.success(apisFacade.assocMockApisAdd(id, dto));
  }

  @ApiOperation(value = "Query the mock apis information associated with the apis", nickname = "apis:association:mock:apis:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<ApisAssocMockApiVo> assocMockApisDetail(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.assocMockApis(id));
  }

  @ApiOperation(value = "Query the detail of apis", nickname = "apis:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisDetailVo> detail(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "resolveRefFlag", value = "Resolve reference flag, default false", required = true)
      @RequestParam(value = "resolveRefFlag", required = false) Boolean resolveRefFlag) {
    return ApiLocaleResult.success(apisFacade.detail(id, resolveRefFlag));
  }

  @ApiOperation(value = "Query the OpenAPI document of apis", nickname = "apis:openapi:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/{id}/openapi")
  public ApiLocaleResult<String> openapiDetail(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      ApisSchemaOpenApiDto dto) {
    return ApiLocaleResult.successData(apisFacade.openapiDetail(id, dto));
  }

  @ApiOperation(value = "Check apis", nickname = "apis:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/check")
  public ApiLocaleResult<?> check(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id) {
    apisFacade.check(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the detail list of apis", nickname = "apis:list:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/list/detail")
  public ApiLocaleResult<List<ApisDetailVo>> listDetail(
      @ApiParam(name = "ids", value = "Apis ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @ApiParam(name = "resolveRefFlag", value = "Resolve reference flag, default false", required = true)
      @RequestParam(value = "resolveRefFlag", required = false) Boolean resolveRefFlag) {
    return ApiLocaleResult.success(apisFacade.listDetail(ids, resolveRefFlag));
  }

  @ApiOperation(value = "Query the basic information of apis", nickname = "apis:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisInfoListVo>> list(@Valid ApisInfoFindDto dto) {
    return ApiLocaleResult.success(apisFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the basic information of apis", nickname = "apis:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisInfoListVo>> search(@Valid ApisInfoSearchDto dto) {
    return ApiLocaleResult.success(apisFacade.search(dto));
  }

  @ApiOperation(value = "Export the OpenAPI specification of apis", nickname = "apis:openapi:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/openapi/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid ApisExportDto dto, HttpServletResponse response) {
    return apisFacade.export(id, dto, response);
  }

}
