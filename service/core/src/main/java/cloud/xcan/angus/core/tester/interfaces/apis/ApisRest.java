package cloud.xcan.angus.core.tester.interfaces.apis;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisInfoSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisMoveDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.mock.ApisAssocMockApisAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.angus.model.apis.ApiStatus;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.validator.constraints.Length;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Apis", description = "API Metadata Management - Central registry for maintaining interface debug value, schema definitions, and version history.")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisRest {

  @Resource
  private ApisFacade apisFacade;

  @Operation(description = "Archive the unarchived apis", operationId = "apis:archive")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful save"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/archive")
  public ApiLocaleResult<List<IdKey<Long, Object>>> archive(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisArchiveDto> dto) {
    return ApiLocaleResult.success(apisFacade.archive(dto));
  }

  @Operation(description = "Update apis", operationId = "apis:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisUpdateDto> dto) {
    apisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace apis", operationId = "apis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisReplaceDto> dto) {
    return ApiLocaleResult.success(apisFacade.replace(dto));
  }

  @Operation(description = "Update the name of apis", operationId = "apis:replace:name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = MAX_NAME_LENGTH_X2) @Parameter(name = "name", description = "New apis name", required = true) @RequestParam("name") String name) {
    apisFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Move the apis to another services", operationId = "apis:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Move successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/move")
  public ApiLocaleResult<?> move(@Valid @RequestBody ApisMoveDto dto) {
    apisFacade.move(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Clone apis", operationId = "apis:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.clone(id));
  }

  @Operation(description = "Delete apis", operationId = "apis:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Apis ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisFacade.delete(ids);
  }

  @Operation(description = "Modify apis status", operationId = "apis:status:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modified successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "Apis status", required = true) @RequestParam("status") ApiStatus status) {
    apisFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(description =
      "Replace server configuration of the apis. A declaration of which servers can be used across the API. "
          + "For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)", operationId = "apis:schema:server:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @PutMapping("/{id}/schema/server")
  public ApiLocaleResult<?> serverReplace(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody Server dto) {
    apisFacade.serverReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(description =
      "Replace all server configuration of the apis. A declaration of which servers can be used across the API. "
          + "Note: `The local server will be deleted when it does not exist in the request`. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)", operationId = "apis:schema:server:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Api not found")
  })
  @PutMapping("/{id}/schema/server/all")
  public ApiLocaleResult<?> serverReplaceAll(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<Server> dto) {
    apisFacade.serverReplaceAll(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete servers of the apis. Delete the servers of apis by url.", operationId = "apis:schema:server:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/schema/server")
  public void serverDelete(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "urls", description = "Server url", required = true) @RequestParam("urls") Set<String> urls) {
    apisFacade.serverDelete(id, urls);
  }

  @Operation(description =
      "Query the server configuration of apis. Note: `The data source includes "
          + "the current api request server, api servers configuration, and parent services servers configuration`.", operationId = "apis:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Api not found")
  })
  @GetMapping("/{id}/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.serverList(id));
  }

  @Operation(description = "Add mock apis association", operationId = "apis:association:mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<IdKey<Long, Object>> assocMockApisAdd(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAssocMockApisAddDto dto) {
    return ApiLocaleResult.success(apisFacade.assocMockApisAdd(id, dto));
  }

  @Operation(description = "Query the mock apis information associated with the apis", operationId = "apis:association:mock:apis:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<ApisAssocMockApiVo> assocMockApisDetail(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.assocMockApis(id));
  }

  @Operation(description = "Query the detail of apis", operationId = "apis:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisDetailVo> detail(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "resolveRef", description = "Resolve reference flag, default false", required = true)
      @RequestParam(value = "resolveRef", required = false) Boolean resolveRef) {
    return ApiLocaleResult.success(apisFacade.detail(id, resolveRef));
  }

  @Operation(description = "Query the OpenAPI document of apis", operationId = "apis:openapi:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @GetMapping(value = "/{id}/openapi")
  public ApiLocaleResult<String> openapiDetail(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ApisSchemaOpenApiDto dto) {
    return ApiLocaleResult.successData(apisFacade.openapiDetail(id, dto));
  }

  @Operation(description = "Check apis", operationId = "apis:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resource existed"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/check")
  public ApiLocaleResult<?> check(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id) {
    apisFacade.check(id);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Query the detail list of apis", operationId = "apis:list:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/list/detail")
  public ApiLocaleResult<List<ApisDetailVo>> listDetail(
      @Parameter(name = "ids", description = "Apis ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @Parameter(name = "resolveRef", description = "Resolve reference flag, default false", required = true)
      @RequestParam(value = "resolveRef", required = false) Boolean resolveRef) {
    return ApiLocaleResult.success(apisFacade.listDetail(ids, resolveRef));
  }

  @Operation(description = "Query the basic information of apis", operationId = "apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisInfoListVo>> list(
      @Valid @ParameterObject ApisInfoFindDto dto) {
    return ApiLocaleResult.success(apisFacade.list(dto));
  }

  @Operation(description = "Fulltext search the basic information of apis", operationId = "apis:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisInfoListVo>> search(
      @Valid @ParameterObject ApisInfoSearchDto dto) {
    return ApiLocaleResult.success(apisFacade.search(dto));
  }

  @Operation(description = "Export the OpenAPI specification of apis", operationId = "apis:openapi:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Exported Successfully")})
  @GetMapping(value = "/{id}/openapi/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "id", description = "Apis id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ApisExportDto dto, HttpServletResponse response) {
    return apisFacade.export(id, dto, response);
  }

}
