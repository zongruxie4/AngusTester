package cloud.xcan.angus.core.tester.interfaces.data;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
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
import java.util.LinkedHashMap;
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

@Tag(name = "Dataset", description = "Test Dataset Management - Comprehensive APIs for creating, editing, organizing, and managing complete test datasets with import/export capabilities and value preview functionality")
@Validated
@RestController
@RequestMapping("/api/v1/dataset")
public class DatasetRest {

  @Resource
  private DatasetFacade datasetFacade;

  @Operation(summary = "Create new dataset", 
      description = "Create a new test dataset with comprehensive configuration and parameter settings",
      operationId = "data:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Dataset created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasetAddDto dto) {
    return ApiLocaleResult.success(datasetFacade.add(dto));
  }

  @Operation(summary = "Update dataset", 
      description = "Update existing dataset configuration and parameters with partial modification support",
      operationId = "data:dataset:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset updated successfully"),
      @ApiResponse(responseCode = "404", description = "Dataset not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody DatasetUpdateDto dto) {
    datasetFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace dataset", 
      description = "Replace existing dataset with complete new configuration and parameter settings",
      operationId = "data:dataset:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Dataset not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody DatasetReplaceDto dto) {
    return ApiLocaleResult.success(datasetFacade.replace(dto));
  }

  @Operation(summary = "Clone datasets", 
      description = "Create copies of multiple datasets with all configuration and parameter settings",
      operationId = "data:dataset:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasets cloned successfully"),
      @ApiResponse(responseCode = "404", description = "One or more datasets not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(datasetFacade.clone(ids));
  }

  @Operation(summary = "Import datasets", 
      description = "Import datasets from external files with comprehensive format support and validation",
      operationId = "data:dataset:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Datasets imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid DatasetImportDto dto) {
    return ApiLocaleResult.success(datasetFacade.imports(dto));
  }

  @Operation(summary = "Import dataset examples", 
      description = "Import predefined dataset examples with sample data and configuration templates",
      operationId = "data:dataset:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Dataset examples imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example dataset import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(datasetFacade.importExample(projectId));
  }

  @Operation(summary = "Delete datasets", 
      description = "Remove multiple datasets from the system with batch operation support",
      operationId = "data:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Datasets deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Dataset identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    datasetFacade.delete(ids);
  }

  @Operation(summary = "Get dataset details", 
      description = "Retrieve comprehensive details and configuration for a specific dataset",
      operationId = "data:dataset:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset details retrieved successfully")})
  @GetMapping("/{id}")
  public ApiLocaleResult<DatasetDetailVo> detail(
      @Parameter(name = "id", description = "Dataset identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasetFacade.detail(id));
  }

  @Operation(summary = "Preview dataset values", 
      description = "Preview dataset parameter values and data content with validation and formatting",
      operationId = "data:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset values preview retrieved successfully")})
  @PostMapping("/value/preview")
  public ApiLocaleResult<LinkedHashMap<String, List<String>>> valuePreview(
      @RequestBody DatasetValuePreviewDto dto) {
    return ApiLocaleResult.success(datasetFacade.valuePreview(dto));
  }

  @Operation(summary = "Query dataset list", 
      description = "Retrieve paginated list of datasets with comprehensive filtering and search options",
      operationId = "data:dataset:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasetDetailVo>> list(
      @Valid @ParameterObject DatasetFindDto dto) {
    return ApiLocaleResult.success(datasetFacade.list(dto));
  }

  @Operation(summary = "Export datasets", 
      description = "Export datasets to external files with comprehensive format support and configuration options",
      operationId = "data:dataset:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasets exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject DatasetExportDto dto, HttpServletResponse response) {
    return datasetFacade.export(dto, response);
  }

}
