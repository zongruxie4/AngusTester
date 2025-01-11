package cloud.xcan.sdf.core.angustester.interfaces.services;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesSchemaFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema.ServiceServerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ServicesSchema")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesSchemaRest {

  @Resource
  private ServicesSchemaFacade servicesSchemaFacade;

  @ApiOperation(value = "Replace the services schema info", nickname = "services:schema:info:replace",
      notes = "Provides metadata about the API. Note: `Metadata is required for an OpenAPI document, so it cannot be deleted after adding it`.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/info")
  public ApiLocaleResult<?> infoReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Info dto) {
    servicesSchemaFacade.infoReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the schema info of services", nickname = "services:schema:info:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{serviceId}/schema/info")
  public ApiLocaleResult<Info> infoDetail(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.infoDetail(serviceId));
  }

  @ApiOperation(value = "Replace the services referencing for external documentation.", nickname = "services:schema:externalDoc:replace",
      notes = "Allows referencing an external resource for extended documentation for OpenAPI document.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/externalDoc")
  public ApiLocaleResult<?> externalDocReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody ExternalDocumentation dto) {
    servicesSchemaFacade.externalDocReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the additional external documentation of services", nickname = "services:schema:externalDoc:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{serviceId}/schema/externalDoc")
  public ApiLocaleResult<ExternalDocumentation> externalDocDetail(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.externalDocDetail(serviceId));
  }

  @ApiOperation(value = "Replace security requirements of the services", nickname = "services:schema:securityRequirement:replace",
      notes = "A declaration of which security mechanisms can be used across the API. For more details on the security requirements, please see: [OpenAPI Specification#Security Requirement Object](https://swagger.io/specification/#security-requirement-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/securityRequirement")
  public ApiLocaleResult<?> securityRequirementReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody SecurityRequirement dto) {
    servicesSchemaFacade.securityRequirementReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace all security requirements of the services", nickname = "services:schema:securityRequirement:all:replace",
      notes = "A declaration of which security mechanisms can be used across the API. For more details on the security requirements, please see: [OpenAPI Specification#Security Requirement Object](https://swagger.io/specification/#security-requirement-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/securityRequirement/all")
  public ApiLocaleResult<?> securityRequirementReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      // Warn:: Swagger-UI caught TypeError: Cannot read properties of undefined (reading 'anyOf')
      @RequestBody List<SecurityRequirement> dtos) {
    servicesSchemaFacade.securityRequirementReplaceAll(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete security requirements of the services", nickname = "services:schema:securityRequirement:delete",
      notes = "Delete the security requirements of services by name")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/securityRequirement")
  public void securityRequirementDelete(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "names", value = "Security schema names", required = true) @RequestParam("names") Set<String> names) {
    servicesSchemaFacade.securityRequirementDelete(serviceId, names);
  }

  @ApiOperation(value = "Query all security requirements of the services", nickname = "services:schema:securityRequirement:all",
      notes = "Query the all security requirement of services")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema/securityRequirement")
  public ApiLocaleResult<List<SecurityRequirement>> securityRequirementList(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.securityRequirementList(serviceId));
  }

  @ApiOperation(value = "Replace server configuration of the services", nickname = "services:schema:server:replace",
      notes = "A declaration of which servers can be used across the API. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/server")
  public ApiLocaleResult<?> serverReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Server dto) {
    servicesSchemaFacade.serverReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Synchronous server configuration to the services apis", nickname = "services:schema:server:apis:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/server/{serverId}/apis/sync")
  public ApiLocaleResult<?> apisServerReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "serverId", value = "Server id", required = true) @PathVariable("serverId") Long serverId) {
    servicesSchemaFacade.apisServerReplace(serviceId, serverId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace all server configurations of the services", nickname = "services:schema:server:all:replace",
      notes = "A declaration of which servers can be used across the API. Note: `The local server will be deleted when it does not exist in the request`. For more details on the server, please see: [OpenAPI Specification#Server Object](https://swagger.io/specification/#server-object)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/server/all")
  public ApiLocaleResult<?> serverReplaceAll(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<Server> dtos) {
    servicesSchemaFacade.serverReplaceAll(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete servers of the services", nickname = "services:schema:server:delete",
      notes = "Delete the servers of services by url")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/server")
  public void serverDelete(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "ids", value = "Server ids", required = true) @RequestParam("ids") Set<Long> ids) {
    servicesSchemaFacade.serverDelete(serviceId, ids);
  }

  @ApiOperation(value = "Query all server configurations of the services", nickname = "services:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverList(serviceId, false));
  }

  @ApiOperation(value = "Query the detail of server configurations", nickname = "services:schema:server:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema/server/{serverId}")
  public ApiLocaleResult<Server> serverDetail(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "serverId", value = "Server id", required = true) @PathVariable("serverId") Long serverId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverDetail(serviceId, serverId));
  }

  @ApiOperation(value = "Query all server configurations of the project services", nickname = "services:schema:server:byProject")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/schema/server")
  public ApiLocaleResult<List<ServiceServerVo>> serverListByProject(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverListByProject(projectId));
  }

  @ApiOperation(value = "Replace tag of the services", nickname = "services:schema:tag:replace",
      notes =
          "Note: `The order of the tags can be used to reflect on their order by the parsing tools`. "
              + "Not all tags that are used by the Operation Object must be declared. The tags that are not declared MAY be organized randomly or based on the tools' logic.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/tag")
  public ApiLocaleResult<?> tagReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Tag dto) {
    servicesSchemaFacade.tagReplace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace all tags of the services", nickname = "services:schema:tag:all:replace",
      notes =
          "Note: `The order of the tags can be used to reflect on their order by the parsing tools`. "
              + "Not all tags that are used by the Operation Object must be declared. The tags that are not declared MAY be organized randomly or based on the tools' logic.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/tag/all")
  public ApiLocaleResult<?> tagReplaceAll(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<Tag> dtos) {
    servicesSchemaFacade.tagReplaceAll(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete tags of the services", nickname = "services:schema:tag:delete",
      notes = "Delete the tags of services by name")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/schema/tag")
  public void tagDelete(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "names", value = "Report names", required = true) @RequestParam("names") Set<String> names) {
    servicesSchemaFacade.tagDelete(serviceId, names);
  }

  @ApiOperation(value = "Query all tags of the services", nickname = "services:schema:tag:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema/tag")
  public ApiLocaleResult<List<Tag>> tagList(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.tagList(serviceId));
  }

  @ApiOperation(value = "Replace all schema extensions of the services", nickname = "services:schema:extensions:all:replace",
      notes = "For more information, please see: [Specification Extensions](https://swagger.io/specification/#specification-extensions)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/schema/extensions")
  public ApiLocaleResult<?> extensionsReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody Map<String, Object> dtos) {
    servicesSchemaFacade.extensionsReplace(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query all schema extensions of the services", nickname = "services:schema:extensions:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema/extensions")
  public ApiLocaleResult<Map<String, Object>> extensionsList(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.extensionsList(serviceId));
  }

  @ApiOperation(value = "Query the schema detail of services", nickname = "services:schema:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/schema")
  public ApiLocaleResult<ServiceSchemaDetailVo> detail(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.detail(serviceId));
  }

  /**
   * @param serviceId       Services id
   * @param forced          When the modification is not forced, if it detects that `Operation
   *                        Object` is deleted or updated, it will stop the modification and return
   *                        a prompt; otherwise, it will directly modify or delete the interface
   *                        that does not exist in OpenAPI, Default false.
   * @param gzipCompression It is recommended to enable gzip compression. After enabling it, the
   *                        data size can be reduced by more than 20 times. By default, gzip
   *                        compression is enabled.
   * @param content         OpenAPI document yaml or json content
   */
  @ApiOperation(value = "Replace the OpenAPI document of services", nickname = "services:openapi:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/openapi")
  public ApiLocaleResult<?> openapiReplace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "forced", value = "Forced modification", required = true) @RequestParam(value = "forced") Boolean forced,
      @ApiParam(name = "gzipCompression", value = "Whether to turn on Gzip compression", required = true) @RequestParam(value = "gzipCompression") Boolean gzipCompression,
      @RequestBody @NotEmpty String content) {
    servicesSchemaFacade.openapiReplace(serviceId, forced, gzipCompression, content);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the OpenAPI document of services", nickname = "services:openapi:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/{serviceId}/openapi")
  public ApiLocaleResult<String> openapiDetail(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      ApisSchemaOpenApiDto dto) {
    return ApiLocaleResult.successData(servicesSchemaFacade.openapiDetail(serviceId, dto));
  }
}
