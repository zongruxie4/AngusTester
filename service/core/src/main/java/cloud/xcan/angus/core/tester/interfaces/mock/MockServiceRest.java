package cloud.xcan.angus.core.tester.interfaces.mock;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceExportDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceFileImportDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceImportDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceServicesAssocDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceListVo;
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

@Tag(name = "Mock Service", description = "Mock Service Management - Unified configuration hub for organizing mock interfaces and virtual runtime environments with full lifecycle management")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceRest {

  @Resource
  private MockServiceFacade mockServiceFacade;

  @Operation(summary = "Create mock service",
      description = "Create new mock service with configuration for API simulation and virtual runtime environment setup",
      operationId = "mock:service:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock service created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody MockServiceAddDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.add(dto));
  }

  @Operation(summary = "Update mock service",
      description = "Update existing mock service configuration with partial modifications for flexible management",
      operationId = "mock:service:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service updated successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody MockServiceUpdateDto dto) {
    mockServiceFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace mock service",
      description = "Replace existing mock service configuration with new definition for updates",
      operationId = "mock:service:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody MockServiceReplaceDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.replace(dto));
  }

  @Operation(summary = "Synchronize mock service to instance",
      description = "Deploy mock service configuration and APIs to mock service instance for runtime availability",
      operationId = "mock:service:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service synchronized to instance successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @Parameter(name = "id", description = "Mock service identifier for instance synchronization", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import mock service from file",
      description = "Import mock service configuration from file with validation and setup",
      operationId = "mock:service:file:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock service imported from file successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/file/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> fileImport(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid MockServiceFileImportDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.fileImport(dto));
  }

  @Operation(summary = "Generate associated mock service from service",
      description = "Create mock service with association to existing service for integrated testing workflows",
      operationId = "mock:service:services:association:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Associated mock service generated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/services")
  public ApiLocaleResult<IdKey<Long, Object>> servicesAssoc(
      @Valid @RequestBody MockServiceServicesAssocDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.servicesAssoc(dto));
  }

  @Operation(summary = "Associate mock service with service",
      description = "Establish association between existing mock service and service for integrated testing workflows",
      operationId = "mock:service:services:association:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock service association updated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PutMapping(value = "/association/{mockServiceId}/{serviceId}")
  public ApiLocaleResult<?> servicesAssocUpdate(
      @Parameter(name = "mockServiceId", description = "Mock service identifier for association", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "serviceId", description = "Service identifier for association", required = true) @PathVariable("serviceId") Long serviceId) {
    mockServiceFacade.servicesAssocUpdate(mockServiceId, serviceId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Start mock service via agent",
      description = "Initiate mock service startup through agent with status monitoring and health checks",
      operationId = "mock:service:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service startup initiated successfully")
  })
  @PostMapping("/start")
  public ApiLocaleResult<List<StartVo>> start(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.start(ids));
  }

  @Operation(summary = "Stop mock service via agent",
      description = "Initiate mock service shutdown through agent with graceful termination and cleanup procedures",
      operationId = "mock:service:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service shutdown initiated successfully")
  })
  @PostMapping("/stop")
  public ApiLocaleResult<List<StopVo>> stop(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.stop(ids));
  }

  @Operation(summary = "Import APIs to existing mock service",
      description = "Import API configurations to existing mock service with validation and setup",
      operationId = "mock:service:apis:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "APIs imported to mock service successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<?> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid MockServiceImportDto dto) {
    mockServiceFacade.imports(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import mock service example",
      description = "Import predefined mock service example with configuration for rapid setup",
      operationId = "mock:service:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service example imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<IdKey<Long, Object>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(mockServiceFacade.importExample(projectId));
  }

  @Operation(summary = "Import mock APIs example",
      description = "Import predefined mock APIs example to existing mock service for rapid API setup",
      operationId = "mock:service:apis:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock APIs example imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/{id}/example/apis/import")
  public ApiLocaleResult<?> importApisExample(
      @Parameter(name = "id", description = "Mock service identifier for example API import", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.importApisExample(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete mock service association",
      description = "Remove association between mock service and API service for relationship management",
      operationId = "mock:service:association:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Mock service association deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/association")
  public void assocDelete(
      @Parameter(name = "id", description = "Mock service identifier for association deletion", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.assocDelete(id);
  }

  @Operation(summary = "Delete mock services",
      description = "Remove mock service definitions from system with cleanup and optional force deletion",
      operationId = "mock:service:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Mock services deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Mock service identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @Parameter(name = "force", description = "Force deletion flag for comprehensive cleanup", required = false)
      @Valid @RequestParam(value = "force", required = false) Boolean force) {
    mockServiceFacade.delete(ids, force);
  }

  @Operation(summary = "Query associated API identifiers",
      description = "Retrieve all associated API identifiers for mock service for relationship management",
      operationId = "mock:service:association:apis:id:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Associated API identifiers retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")})
  @GetMapping(value = "/{id}/association/apis/id")
  public ApiLocaleResult<Set<Long>> assocApisIdsList(
      @Parameter(name = "id", description = "Mock service identifier for API association query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.assocApisIdsList(id));
  }

  @Operation(summary = "Query mock service details",
      description = "Retrieve information about specific mock service including configuration and status",
      operationId = "mock:service:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock service not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockServiceDetailVo> detail(
      @Parameter(name = "id", description = "Mock service identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.detail(id));
  }

  @Operation(summary = "Query mock services with filtering",
      description = "Retrieve paginated list of mock services with filtering and search capabilities",
      operationId = "mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock services retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<MockServiceListVo>> list(
      @Valid @ParameterObject MockServiceFindDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.list(dto));
  }

  @DoInFuture("Limit the number of exports")
  @Operation(summary = "Export APIs from mock service",
      description = "Export API configurations from existing mock service with formatting options",
      operationId = "mock:service:apis:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "APIs exported from mock service successfully")})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject MockServiceExportDto dto, HttpServletResponse response) {
    return mockServiceFacade.export(dto, response);
  }

}
