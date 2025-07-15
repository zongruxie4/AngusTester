package cloud.xcan.angus.core.tester.interfaces.services;

import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.assembleAllowImportSampleStatus;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
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

@Tag(name = "Services", description = "Api Service Registry - Unified lifecycle management for API services.")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesRest {

  @Resource
  private ServicesFacade serviceFacade;

  @Operation(summary = "Add services", operationId = "services:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ServicesAddDto dto) {
    return ApiLocaleResult.success(serviceFacade.add(dto));
  }

  @Operation(summary = "Replace the name of services", operationId = "services:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New services name", required = true) @RequestParam(value = "name") String name) {
    serviceFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Modify services status. Note: When modifying a services, "
      + "all services and apis status under the services will be synchronously modified.", operationId = "services:status:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modified successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "Services status", required = true) @RequestParam("status") ApiStatus status) {
    serviceFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone services", operationId = "services:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cloned clone"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<?> clone(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id) {
    serviceFacade.clone(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import the apis to services", operationId = "services:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid ServicesImportDto dto) {
    return ApiLocaleResult.success(serviceFacade.imports(dto));
  }

  @Operation(summary = "Import the inner services example", operationId = "services:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(serviceFacade.importExample(projectId));
  }

  @Operation(summary = "Delete services", operationId = "services:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id) {
    serviceFacade.delete(id);
  }

  @Operation(summary = "Query the detail of services", operationId = "services:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ServicesDetailVo> detail(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "joinSchema", description = "Whether to associate services schema information flag", required = false)
      @RequestParam(name = "joinSchema", required = false) Boolean joinSchema) {
    return ApiLocaleResult.success(serviceFacade.detail(id, joinSchema));
  }

  @Operation(summary = "Query the Mock service information associated with a services", operationId = "services:association:mock:service")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/association/mock/service")
  public ApiLocaleResult<MockServiceDetailVo> associationMockService(
      @Parameter(name = "id", description = "Services id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(serviceFacade.associationMockService(id));
  }

  @Operation(summary = "Query the list of services", operationId = "services:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ServiceVo>> list(@Valid @ParameterObject ServicesFindDto dto) {
    PageResult<ServiceVo> result = serviceFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @Operation(summary = "Fulltext search the services", operationId = "services:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ServiceVo>> search(
      @Valid @ParameterObject ServicesSearchDto dto) {
    PageResult<ServiceVo> result = serviceFacade.search(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @DoInFuture("Limit the number of exports")
  @Operation(summary = "Export the apis from services", operationId = "services:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Exported Successfully")})
  @PostMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      /* Fix export service 414 error */
      @Valid @RequestBody ServicesExportDto dto, HttpServletResponse response) {
    return serviceFacade.export(dto, response);
  }

}
