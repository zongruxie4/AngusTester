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
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceSearchDto;
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

@Tag(name = "MockService", description = "Mock Service Management - Unified configuration hub for organizing mock interfaces and virtual runtime environments.")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceRest {

  @Resource
  private MockServiceFacade mockServiceFacade;

  @Operation(summary = "Add mock service", operationId = "mock:service:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody MockServiceAddDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.add(dto));
  }

  @Operation(summary = "Update mock service", operationId = "mock:service:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody MockServiceUpdateDto dto) {
    mockServiceFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace mock service", operationId = "mock:service:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody MockServiceReplaceDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.replace(dto));
  }

  @Operation(summary = "Sync the service setting and apis to mock service instance", operationId = "mock:service:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Synchronized successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import the mock service from file", operationId = "mock:service:file:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/file/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> fileImport(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid MockServiceFileImportDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.fileImport(dto));
  }

  @Operation(summary = "Generate associated mock service based on the services", operationId = "mock:service:services:association:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Associated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/services")
  public ApiLocaleResult<IdKey<Long, Object>> servicesAssoc(
      @Valid @RequestBody MockServiceServicesAssocDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.servicesAssoc(dto));
  }

  @Operation(summary = "Associated mock service based on the services", operationId = "mock:service:services:association:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Associated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PutMapping(value = "/association/{mockServiceId}/{serviceId}")
  public ApiLocaleResult<?> servicesAssocUpdate(
      @Parameter(name = "mockServiceId", description = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    mockServiceFacade.servicesAssocUpdate(mockServiceId, serviceId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Start mock service by agent", operationId = "mock:service:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully processed")
  })
  @PostMapping("/start")
  public ApiLocaleResult<List<StartVo>> start(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.start(ids));
  }

  @Operation(summary = "Stop mock service by agent", operationId = "mock:service:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully processed")
  })
  @PostMapping("/stop")
  public ApiLocaleResult<List<StopVo>> stop(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.stop(ids));
  }

  @Operation(summary = "Import the apis to existed mock service", operationId = "mock:service:apis:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<?> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid MockServiceImportDto dto) {
    mockServiceFacade.imports(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import the inner mock service example", operationId = "mock:service:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<IdKey<Long, Object>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(mockServiceFacade.importExample(projectId));
  }

  @Operation(summary = "Import the inner mock apis example", operationId = "mock:service:apis:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/{id}/example/apis/import")
  public ApiLocaleResult<?> importApisExample(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.importApisExample(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the association between mock service and project", operationId = "mock:service:association:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/association")
  public void assocDelete(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.assocDelete(id);
  }

  @Operation(summary = "Delete mock services", operationId = "mock:service:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Mock service ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @Parameter(name = "force", description = "Force deletion flag", required = false)
      @Valid @RequestParam(value = "force", required = false) Boolean force) {
    mockServiceFacade.delete(ids, force);
  }

  @Operation(summary = "Query the associated apis ids of mock service", operationId = "mock:service:association:apis:id:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/association/apis/id")
  public ApiLocaleResult<Set<Long>> assocApisIdsList(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.assocApisIdsList(id));
  }

  @Operation(summary = "Query the detail of mock service", operationId = "mock:service:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockServiceDetailVo> detail(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.detail(id));
  }

  @Operation(summary = "Query the list of mock service", operationId = "mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<MockServiceListVo>> list(
      @Valid @ParameterObject MockServiceFindDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of mock service", operationId = "mock:service:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<MockServiceListVo>> search(
      @Valid @ParameterObject MockServiceSearchDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.search(dto));
  }

  @DoInFuture("Limit the number of exports")
  @Operation(summary = "Export the apis from existed mock service", operationId = "mock:service:apis:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Exported Successfully")})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject MockServiceExportDto dto, HttpServletResponse response) {
    return mockServiceFacade.export(dto, response);
  }

}
