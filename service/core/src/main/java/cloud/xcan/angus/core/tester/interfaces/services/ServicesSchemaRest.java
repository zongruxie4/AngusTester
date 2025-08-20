package cloud.xcan.angus.core.tester.interfaces.services;


import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesSchemaFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceServerVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Services Schema",
    description = "API Metadata and Schema Management API - Management system for OpenAPI metadata and JSON Schema definitions with synchronization capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesSchemaRest {

  @Resource
  private ServicesSchemaFacade servicesSchemaFacade;

  @Operation(summary = "Replace service schema info",
      description = "Configure metadata information for the API. Note: `Metadata is required for an OpenAPI document, so it cannot be deleted after adding it`",
      operationId = "services:schema:info:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service schema info replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/info")
  public ApiLocaleResult<?> infoReplace(
      @Parameter(name = "serviceId", description = "Service identifier for schema configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Info dto) {
    servicesSchemaFacade.infoReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service schema info",
      description = "Retrieve metadata information for service API documentation",
      operationId = "services:schema:info:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service schema info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @GetMapping(value = "/{serviceId}/schema/info")
  public ApiLocaleResult<Info> infoDetail(
      @Parameter(name = "serviceId", description = "Service identifier for schema query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.infoDetail(serviceId));
  }

  @Operation(summary = "Replace service external documentation reference",
      description = "Configure external resource references for extended API documentation",
      operationId = "services:schema:externalDoc:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service external documentation replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/externalDoc")
  public ApiLocaleResult<?> externalDocReplace(
      @Parameter(name = "serviceId", description = "Service identifier for external documentation configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody ExternalDocumentation dto) {
    servicesSchemaFacade.externalDocReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service external documentation",
      description = "Retrieve external documentation configuration for service API",
      operationId = "services:schema:externalDoc:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service external documentation retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @GetMapping(value = "/{serviceId}/schema/externalDoc")
  public ApiLocaleResult<ExternalDocumentation> externalDocDetail(
      @Parameter(name = "serviceId", description = "Service identifier for external documentation query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.externalDocDetail(serviceId));
  }

  @Operation(summary = "Replace service security requirements",
      description = "Configure security mechanisms for API access control. For more details on the security requirements, please see: [OpenAPI Specification#Security Requirement Object](https://swagger.io/specification/#security-requirement-object)",
      operationId = "services:schema:securityRequirement:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service security requirements replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/securityRequirement")
  public ApiLocaleResult<?> securityRequirementReplace(
      @Parameter(name = "serviceId", description = "Service identifier for security configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody SecurityRequirement dto) {
    servicesSchemaFacade.securityRequirementReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace all service security requirements",
      description = "Configure all security mechanisms for API access control. For more details on the security requirements, please see: [OpenAPI Specification#Security Requirement Object](https://swagger.io/specification/#security-requirement-object)",
      operationId = "services:schema:securityRequirement:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service security requirements replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/securityRequirement/all")
  public ApiLocaleResult<?> securityRequirementReplace(
      @Parameter(name = "serviceId", description = "Service identifier for security configuration", required = true) @PathVariable("serviceId") Long serviceId,
      // Warn:: Swagger-UI caught TypeError: Cannot read properties of undefined (reading 'anyOf')
      @RequestBody List<SecurityRequirement> dto) {
    servicesSchemaFacade.securityRequirementReplaceAll(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service security requirements",
      description = "Remove security requirements by schema names for service access control",
      operationId = "services:schema:securityRequirement:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service security requirements deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/securityRequirement")
  public void securityRequirementDelete(
      @Parameter(name = "serviceId", description = "Service identifier for security configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "names", description = "Security schema names for deletion", required = true) @RequestParam("names") Set<String> names) {
    servicesSchemaFacade.securityRequirementDelete(serviceId, names);
  }

  @Operation(summary = "Query all service security requirements",
      description = "Retrieve all security requirements for service access control configuration",
      operationId = "services:schema:securityRequirement:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service security requirements retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema/securityRequirement")
  public ApiLocaleResult<List<SecurityRequirement>> securityRequirementList(
      @Parameter(name = "serviceId", description = "Service identifier for security query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.securityRequirementList(serviceId));
  }

  @Operation(summary = "Replace service server configuration",
      description = "Configure server settings for API execution environment. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)",
      operationId = "services:schema:server:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service server configuration replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/server")
  public ApiLocaleResult<?> serverReplace(
      @Parameter(name = "serviceId", description = "Service identifier for server configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Server dto) {
    servicesSchemaFacade.serverReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Synchronize server configuration to service APIs",
      description = "Apply server configuration to all APIs under the service for consistent execution environment",
      operationId = "services:schema:server:apis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Server configuration synchronized successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/server/{serverId}/apis/sync")
  public ApiLocaleResult<?> apisServerReplace(
      @Parameter(name = "serviceId", description = "Service identifier for server synchronization", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "serverId", description = "Server identifier for synchronization", required = true) @PathVariable("serverId") Long serverId) {
    servicesSchemaFacade.apisServerReplace(serviceId, serverId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace all service server configurations",
      description = "Configure all server settings for API execution environment. Note: `The local server will be deleted when it does not exist in the request`. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)",
      operationId = "services:schema:server:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service server configurations replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/server/all")
  public ApiLocaleResult<?> serverReplaceAll(
      @Parameter(name = "serviceId", description = "Service identifier for server configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<Server> dto) {
    servicesSchemaFacade.serverReplaceAll(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service servers",
      description = "Remove server configurations by identifiers for service execution environment",
      operationId = "services:schema:server:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service servers deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/server")
  public void serverDelete(
      @Parameter(name = "serviceId", description = "Service identifier for server configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "ids", description = "Server identifiers for deletion", required = true) @RequestParam("ids") Set<Long> ids) {
    servicesSchemaFacade.serverDelete(serviceId, ids);
  }

  @Operation(summary = "Query all service server configurations",
      description = "Retrieve all server configurations for service execution environment",
      operationId = "services:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service server configurations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @Parameter(name = "serviceId", description = "Service identifier for server query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverList(serviceId, false));
  }

  @Operation(summary = "Query service server detail configuration",
      description = "Retrieve detailed server configuration for service execution environment",
      operationId = "services:schema:server:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service server detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema/server/{serverId}")
  public ApiLocaleResult<Server> serverDetail(
      @Parameter(name = "serviceId", description = "Service identifier for server query", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "serverId", description = "Server identifier for detail query", required = true) @PathVariable("serverId") Long serverId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverDetail(serviceId, serverId));
  }

  @Operation(summary = "Query all project server configurations",
      description = "Retrieve all server configurations across project services for execution environment management",
      operationId = "services:schema:server:byProject")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All project server configurations retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/schema/server")
  public ApiLocaleResult<List<ServiceServerVo>> serverListByProject(
      @Parameter(name = "projectId", description = "Project identifier for server query", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverListByProject(projectId));
  }

  @Operation(summary = "Replace service tag",
      description = "Configure API tag for service documentation organization. Note: `The order of the tags can be used to reflect on their order by the parsing tools.` Not all tags that are used by the Operation Object must be declared. The tags that are not declared MAY be organized randomly or based on the tools' logic",
      operationId = "services:schema:tag:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service tag replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/tag")
  public ApiLocaleResult<?> tagReplace(
      @Parameter(name = "serviceId", description = "Service identifier for tag configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Tag dto) {
    servicesSchemaFacade.tagReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace all service tags",
      description = "Configure all API tags for service documentation organization. Note: `The order of the tags can be used to reflect on their order by the parsing tools.` Not all tags that are used by the Operation Object must be declared. The tags that are not declared MAY be organized randomly or based on the tools' logic",
      operationId = "services:schema:tag:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service tags replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/tag/all")
  public ApiLocaleResult<?> tagReplaceAll(
      @Parameter(name = "serviceId", description = "Service identifier for tag configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<Tag> dto) {
    servicesSchemaFacade.tagReplaceAll(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service tags",
      description = "Remove API tags by names for service documentation organization",
      operationId = "services:schema:tag:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service tags deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/tag")
  public void tagDelete(
      @Parameter(name = "serviceId", description = "Service identifier for tag configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "names", description = "Tag names for deletion", required = true) @RequestParam("names") Set<String> names) {
    servicesSchemaFacade.tagDelete(serviceId, names);
  }

  @Operation(summary = "Query all service tags",
      description = "Retrieve all API tags for service documentation organization",
      operationId = "services:schema:tag:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service tags retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema/tag")
  public ApiLocaleResult<List<Tag>> tagList(
      @Parameter(name = "serviceId", description = "Service identifier for tag query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.tagList(serviceId));
  }

  @Operation(summary = "Replace all service schema extensions",
      description = "Configure schema extensions for service API specification. For more information, please see: [Specification Extensions](https://swagger.io/specification/#specification-extensions)",
      operationId = "services:schema:extensions:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service schema extensions replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/schema/extensions")
  public ApiLocaleResult<?> extensionsReplace(
      @Parameter(name = "serviceId", description = "Service identifier for extension configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Map<String, Object> dto) {
    servicesSchemaFacade.extensionsReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query all service schema extensions",
      description = "Retrieve all schema extensions for service API specification",
      operationId = "services:schema:extensions:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service schema extensions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema/extensions")
  public ApiLocaleResult<Map<String, Object>> extensionsList(
      @Parameter(name = "serviceId", description = "Service identifier for extension query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.extensionsList(serviceId));
  }

  @Operation(summary = "Query service schema detail",
      description = "Retrieve schema configuration and metadata for service API",
      operationId = "services:schema:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service schema detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping("/{serviceId}/schema")
  public ApiLocaleResult<ServiceSchemaDetailVo> detail(
      @Parameter(name = "serviceId", description = "Service identifier for schema query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.detail(serviceId));
  }

  /**
   * @param serviceId       Service identifier
   * @param forced          When the modification is not forced, if it detects that `Operation Object` is deleted or updated, it will stop the modification and return a prompt; otherwise, it will directly modify or delete the interface that does not exist in OpenAPI, Default false.
   * @param gzipCompression It is recommended to enable gzip compression. After enabling it, the data size can be reduced by more than 20 times. By default, gzip compression is enabled.
   * @param content         OpenAPI document yaml or json content
   */
  @Operation(summary = "Replace service OpenAPI document",
      description = "Replace OpenAPI document configuration for service with validation and processing options",
      operationId = "services:openapi:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service OpenAPI document replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/openapi")
  public ApiLocaleResult<?> openapiReplace(
      @Parameter(name = "serviceId", description = "Service identifier for OpenAPI configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "forced", description = "Flag for forced modification", required = true) @RequestParam(value = "forced") Boolean forced,
      @Parameter(name = "gzipCompression", description = "Flag to enable Gzip compression", required = true) @RequestParam(value = "gzipCompression") Boolean gzipCompression,
      @RequestBody @NotEmpty String content) {
    servicesSchemaFacade.openapiReplace(serviceId, forced, gzipCompression, content);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query service OpenAPI document",
      description = "Retrieve OpenAPI document configuration for service with format customization",
      operationId = "services:openapi:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service OpenAPI document retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @GetMapping(value = "/{serviceId}/openapi")
  public ApiLocaleResult<String> openapiDetail(
      @Parameter(name = "serviceId", description = "Service identifier for OpenAPI query", required = true) @PathVariable("serviceId") Long serviceId,
      @ParameterObject ApisSchemaOpenApiDto dto) {
    return ApiLocaleResult.successData(servicesSchemaFacade.openapiDetail(serviceId, dto));
  }

  @Operation(summary = "Translate OpenAPI specification text",
      description = "Translate OpenAPI specification content between supported languages for internationalization",
      operationId = "services:schema:translate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OpenAPI specification translated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PatchMapping("/{id}/translate")
  public ApiLocaleResult<?> translate(
      @Parameter(name = "id", description = "Service identifier for translation", required = true) @PathVariable("id") Long id,
      @Parameter(name = "sourceLanguage", description = "Source language for translation", required = true) @RequestParam("sourceLanguage") SupportedLanguage sourceLanguage,
      @Parameter(name = "targetLanguage", description = "Target language for translation", required = true) @RequestParam("targetLanguage") SupportedLanguage targetLanguage) {
    servicesSchemaFacade.translate(id, sourceLanguage, targetLanguage);
    return ApiLocaleResult.success();
  }
}
