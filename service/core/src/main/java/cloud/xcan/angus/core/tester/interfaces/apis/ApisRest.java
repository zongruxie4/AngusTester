package cloud.xcan.angus.core.tester.interfaces.apis;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisInfoFindDto;
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

@Tag(name = "APIs", description = "APIs Management - APIs for interface registry maintenance, schema definitions, version history, and API lifecycle management with debug value tracking")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisRest {

  @Resource
  private ApisFacade apisFacade;

  @Operation(summary = "Archive unarchived APIs", 
      description = "Move unarchived APIs to archive with version control and lifecycle management",
      operationId = "apis:archive")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs archived successfully"),
      @ApiResponse(responseCode = "404", description = "One or more APIs not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/archive")
  public ApiLocaleResult<List<IdKey<Long, Object>>> archive(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisArchiveDto> dto) {
    return ApiLocaleResult.success(apisFacade.archive(dto));
  }

  @Operation(summary = "Update APIs", 
      description = "Update multiple APIs with configuration and validation",
      operationId = "apis:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs updated successfully"),
      @ApiResponse(responseCode = "404", description = "One or more APIs not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisUpdateDto> dto) {
    apisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace APIs", 
      description = "Replace multiple APIs with new configuration and validation",
      operationId = "apis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs replaced successfully")})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisReplaceDto> dto) {
    return ApiLocaleResult.success(apisFacade.replace(dto));
  }

  @Operation(summary = "Rename API", 
      description = "Update API name with validation and metadata management",
      operationId = "apis:replace:name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API name updated successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "API identifier for name update", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = MAX_NAME_LENGTH_X2) @Parameter(name = "name", description = "New API name for identification", required = true) @RequestParam("name") String name) {
    apisFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Move API to another service", 
      description = "Relocate API to different service with validation and relationship management",
      operationId = "apis:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API moved successfully"),
      @ApiResponse(responseCode = "404", description = "API or target service not found")
  })
  @PatchMapping("/move")
  public ApiLocaleResult<?> move(@Valid @RequestBody ApisMoveDto dto) {
    apisFacade.move(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone API", 
      description = "Create copy of API with all configuration, schema definitions, and metadata",
      operationId = "apis:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API cloned successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "API identifier for cloning", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.clone(id));
  }

  @Operation(summary = "Delete APIs", 
      description = "Remove multiple APIs from the system with batch operation support and cleanup",
      operationId = "apis:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "APIs deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "API identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisFacade.delete(ids);
  }

  @Operation(summary = "Update API status", 
      description = "Modify API status with lifecycle management and validation",
      operationId = "apis:status:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API status updated successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @Parameter(name = "id", description = "API identifier for status update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "New API status for lifecycle management", required = true) @RequestParam("status") ApiStatus status) {
    apisFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace API server configuration", 
      description = "Update API server configuration with validation and deployment settings",
      operationId = "apis:schema:server:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API server configuration replaced successfully"),
      @ApiResponse(responseCode = "404", description = "API service not found")
  })
  @PutMapping("/{id}/schema/server")
  public ApiLocaleResult<?> serverReplace(
      @Parameter(name = "id", description = "API identifier for server configuration", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody Server dto) {
    apisFacade.serverReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace all API server configurations", 
      description = "Update all API server configurations with validation and deployment settings",
      operationId = "apis:schema:server:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All API server configurations replaced successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")
  })
  @PutMapping("/{id}/schema/server/all")
  public ApiLocaleResult<?> serverReplaceAll(
      @Parameter(name = "id", description = "API identifier for server configuration update", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<Server> dto) {
    apisFacade.serverReplaceAll(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API servers", 
      description = "Remove specific servers from API configuration with cleanup",
      operationId = "apis:schema:server:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API servers deleted successfully"),
      @ApiResponse(responseCode = "404", description = "API service not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/schema/server")
  public void serverDelete(
      @Parameter(name = "id", description = "API identifier for server deletion", required = true) @PathVariable("id") Long id,
      @Parameter(name = "urls", description = "Server URLs for targeted deletion", required = true) @RequestParam("urls") Set<String> urls) {
    apisFacade.serverDelete(id, urls);
  }

  @Operation(summary = "Get API server configuration", 
      description = "Retrieve server configuration including current API request server, servers configuration, and parent services servers configuration",
      operationId = "apis:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API server configuration retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")
  })
  @GetMapping("/{id}/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "id", description = "API identifier for server configuration query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.serverList(id));
  }

  @Operation(summary = "Add mock API association", 
      description = "Establish association between API and mock APIs with configuration",
      operationId = "apis:association:mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock API association created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<IdKey<Long, Object>> assocMockApisAdd(
      @Parameter(name = "id", description = "API identifier for mock association", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ApisAssocMockApisAddDto dto) {
    return ApiLocaleResult.success(apisFacade.assocMockApisAdd(id, dto));
  }

  @Operation(summary = "Get mock API associations", 
      description = "Retrieve mock API associations for specific API with detailed information",
      operationId = "apis:association:mock:apis:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API associations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @GetMapping(value = "/{id}/association/mock/apis")
  public ApiLocaleResult<ApisAssocMockApiVo> assocMockApisDetail(
      @Parameter(name = "id", description = "API identifier for mock association query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisFacade.assocMockApis(id));
  }

  @Operation(summary = "Get API details", 
      description = "Retrieve API details including schema definitions, metadata, and configuration",
      operationId = "apis:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisDetailVo> detail(
      @Parameter(name = "id", description = "API identifier for detail retrieval", required = true) @PathVariable("id") Long id,
      @Parameter(name = "resolveRef", description = "Reference resolution flag for enhanced schema processing", required = true)
      @RequestParam(value = "resolveRef", required = false) Boolean resolveRef) {
    return ApiLocaleResult.success(apisFacade.detail(id, resolveRef));
  }

  @Operation(summary = "Get API OpenAPI document", 
      description = "Retrieve OpenAPI specification document for specific API with format support",
      operationId = "apis:openapi:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API OpenAPI document retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API service not found")
  })
  @GetMapping(value = "/{id}/openapi")
  public ApiLocaleResult<String> openapiDetail(
      @Parameter(name = "id", description = "API identifier for OpenAPI document", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ApisSchemaOpenApiDto dto) {
    return ApiLocaleResult.successData(apisFacade.openapiDetail(id, dto));
  }

  @Operation(summary = "Check API existence", 
      description = "Validate API existence with resource validation",
      operationId = "apis:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API exists and is valid"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/check")
  public ApiLocaleResult<?> check(
      @Parameter(name = "id", description = "API identifier for existence check", required = true) @PathVariable("id") Long id) {
    apisFacade.check(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get API details list", 
      description = "Retrieve details for multiple APIs with batch operation support",
      operationId = "apis:list:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API details list retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more APIs not found")})
  @GetMapping(value = "/list/detail")
  public ApiLocaleResult<List<ApisDetailVo>> listDetail(
      @Parameter(name = "ids", description = "API identifiers for detail retrieval", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @Parameter(name = "resolveRef", description = "Reference resolution flag for enhanced schema processing", required = true)
      @RequestParam(value = "resolveRef", required = false) Boolean resolveRef) {
    return ApiLocaleResult.success(apisFacade.listDetail(ids, resolveRef));
  }

  @Operation(summary = "Query API list", 
      description = "Retrieve paginated list of APIs with filtering, search options, and basic information",
      operationId = "apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisInfoListVo>> list(
      @Valid @ParameterObject ApisInfoFindDto dto) {
    return ApiLocaleResult.success(apisFacade.list(dto));
  }

  @Operation(summary = "Export API OpenAPI specification", 
      description = "Export API to OpenAPI specification format with configuration options and format support",
      operationId = "apis:openapi:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API OpenAPI specification exported successfully")})
  @GetMapping(value = "/{id}/openapi/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Parameter(name = "id", description = "API identifier for OpenAPI export", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ApisExportDto dto, HttpServletResponse response) {
    return apisFacade.export(id, dto, response);
  }

}
