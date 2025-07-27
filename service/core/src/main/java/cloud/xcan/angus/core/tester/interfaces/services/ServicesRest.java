package cloud.xcan.angus.core.tester.interfaces.services;

import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.assembleAllowImportSampleStatus;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.DoInFuture;
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
import java.util.List;
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

@Tag(name = "Services", description = "API Service Registry Management API - Comprehensive lifecycle management system for API services with import/export capabilities.")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesRest {

  @Resource
  private ServicesFacade serviceFacade;

  @Operation(summary = "Create service",
      description = "Create new API service with comprehensive configuration and metadata.",
      operationId = "services:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Service created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ServicesAddDto dto) {
    return ApiLocaleResult.success(serviceFacade.add(dto));
  }

  @Operation(summary = "Replace service name",
      description = "Update service name for identification and management purposes.",
      operationId = "services:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service name replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Service identifier for name update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New service name for identification", required = true) @RequestParam(value = "name") String name) {
    serviceFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update service status",
      description = "Modify service status with cascading updates to all associated services and APIs.",
      operationId = "services:status:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @Parameter(name = "id", description = "Service identifier for status update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "Service status for lifecycle management", required = true) @RequestParam("status") ApiStatus status) {
    serviceFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone service",
      description = "Create duplicate service with new identifier while preserving original configuration.",
      operationId = "services:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Service cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<?> clone(
      @Parameter(name = "id", description = "Service identifier for cloning", required = true) @PathVariable("id") Long id) {
    serviceFacade.clone(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import APIs to service",
      description = "Import API specifications to service with format validation and processing.",
      operationId = "services:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid ServicesImportDto dto) {
    return ApiLocaleResult.success(serviceFacade.imports(dto));
  }

  @Operation(summary = "Import service example",
      description = "Import predefined service examples for demonstration and testing purposes.",
      operationId = "services:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service examples imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(serviceFacade.importExample(projectId));
  }

  @Operation(summary = "Delete service",
      description = "Remove service and all associated configurations from the system.",
      operationId = "services:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Service identifier for deletion", required = true) @PathVariable("id") Long id) {
    serviceFacade.delete(id);
  }

  @Operation(summary = "Query service detail",
      description = "Retrieve comprehensive service configuration and metadata with optional schema information.",
      operationId = "services:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ServicesDetailVo> detail(
      @Parameter(name = "id", description = "Service identifier for detail query", required = true) @PathVariable("id") Long id,
      @Parameter(name = "joinSchema", description = "Flag to include service schema information in response") @RequestParam(name = "joinSchema", required = false) Boolean joinSchema) {
    return ApiLocaleResult.success(serviceFacade.detail(id, joinSchema));
  }

  @Operation(summary = "Query associated mock service information",
      description = "Retrieve mock service configuration associated with the service for testing purposes.",
      operationId = "services:association:mock:service")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Associated mock service information retrieved successfully")})
  @GetMapping(value = "/{id}/association/mock/service")
  public ApiLocaleResult<MockServiceDetailVo> associationMockService(
      @Parameter(name = "id", description = "Service identifier for mock service query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(serviceFacade.associationMockService(id));
  }

  @Operation(summary = "Query services list",
      description = "Retrieve paginated list of services with comprehensive filtering and search capabilities.",
      operationId = "services:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Services list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ServiceVo>> list(@Valid @ParameterObject ServicesFindDto dto) {
    PageResult<ServiceVo> result = serviceFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @DoInFuture("Limit the number of exports")
  @Operation(summary = "Export APIs from service",
      description = "Export API specifications from service with format customization and selective data export.",
      operationId = "services:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "APIs exported successfully")})
  @PostMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      /* Fix export service 414 error */
      @Valid @RequestBody ServicesExportDto dto, HttpServletResponse response) {
    return serviceFacade.export(dto, response);
  }

}
