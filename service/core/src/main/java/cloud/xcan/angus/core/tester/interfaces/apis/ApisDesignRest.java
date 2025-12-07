package cloud.xcan.angus.core.tester.interfaces.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisDesignFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignContentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignExportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignImportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignUpdateNameDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "API Design", description = "API Design Management - APIs for OpenAPI specification development, version control, compliance monitoring, and service generation with design lifecycle management")
@Validated
@RestController
@RequestMapping("/api/v1/apis/design")
public class ApisDesignRest {

  @Resource
  private ApisDesignFacade apisDesignFacade;

  @Operation(summary = "Create API design",
      description = "Create new API design specification with OpenAPI configuration and version control",
      operationId = "apis:design:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API design created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ApisDesignAddDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.add(dto));
  }

  @Operation(summary = "Update API design name",
      description = "Update API design name with version control and metadata management",
      operationId = "apis:design:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design name updated successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @PutMapping
  public ApiLocaleResult<?> updateName(@Valid @RequestBody ApisDesignUpdateNameDto dto) {
    apisDesignFacade.updateName(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace API design content",
      description = "Replace API design content with new OpenAPI specification and version control",
      operationId = "apis:design:content:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design content replaced successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @PutMapping("/content")
  public ApiLocaleResult<?> replaceContent(@Valid @RequestBody ApisDesignContentReplaceDto dto) {
    apisDesignFacade.replaceContent(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Release API design",
      description = "Release API design specification with version control and service generation",
      operationId = "apis:design:release")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design released successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @PutMapping("/{id}/release")
  public ApiLocaleResult<?> release(
      @Parameter(name = "id", description = "API design identifier for release", required = true) @PathVariable("id") Long id) {
    apisDesignFacade.release(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone API design",
      description = "Create copy of API design with all configuration and version control settings",
      operationId = "apis:design:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design cloned successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @PutMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "API design identifier for cloning", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisDesignFacade.clone(id));
  }

  @Operation(summary = "Associate API design with service",
      description = "Establish association between API design and existing service with integration",
      operationId = "apis:design:services:associate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design service association created successfully"),
      @ApiResponse(responseCode = "404", description = "API service not found")})
  @PutMapping("/services/{id}/associate")
  public ApiLocaleResult<?> servicesAssociate(
      @Parameter(name = "id", description = "API service identifier for design association", required = true) @PathVariable("id") Long serviceId) {
    apisDesignFacade.servicesAssociate(serviceId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Generate services from API design",
      description = "Generate API services from design specification with service creation and configuration",
      operationId = "apis:design:services:generate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API services generated successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @PutMapping("/{id}/services/generate")
  public ApiLocaleResult<?> servicesGenerate(
      @Parameter(name = "id", description = "API design identifier for service generation", required = true) @PathVariable("id") Long id) {
    apisDesignFacade.servicesGenerate(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import API design",
      description = "Import API design from external files with format support and validation",
      operationId = "apis:design:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid ApisDesignImportDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.imports(dto));
  }

  @Operation(summary = "Delete API designs",
      description = "Remove multiple API designs from the system with batch operation support",
      operationId = "apis:design:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API designs deleted successfully"),
      @ApiResponse(responseCode = "404", description = "One or more API designs not found")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "API design identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisDesignFacade.delete(ids);
  }

  @Operation(summary = "Get API design details",
      description = "Retrieve API design details including specification, version control, and metadata",
      operationId = "space:design:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API design not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisDesignDetailVo> detail(
      @Parameter(name = "id", description = "API design identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisDesignFacade.detail(id));
  }

  @Operation(summary = "Query API design list",
      description = "Retrieve paginated list of API designs with filtering and search options",
      operationId = "apis:design:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API design list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisDesignVo>> list(
      @Valid @ParameterObject ApisDesignFindDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.list(dto));
  }

  @Operation(summary = "Export API design specification",
      description = "Export API design to OpenAPI specification format with configuration options",
      operationId = "apis:design:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API design specification exported successfully")})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject ApisDesignExportDto dto, HttpServletResponse response) {
    return apisDesignFacade.export(dto, response);
  }

}
