package cloud.xcan.angus.core.tester.interfaces.data;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
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

@Tag(name = "DataDataset", description = "Test Dataset Management - Centralized control interface for creating, editing, and organizing complete test datasets.")
@Validated
@RestController
@RequestMapping("/api/v1/dataset")
public class DatasetRest {

  @Resource
  private DatasetFacade datasetFacade;

  @Operation(summary = "Add dataset", operationId = "data:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasetAddDto dto) {
    return ApiLocaleResult.success(datasetFacade.add(dto));
  }

  @Operation(summary = "Update dataset", operationId = "data:dataset:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody DatasetUpdateDto dto) {
    datasetFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace dataset", operationId = "data:dataset:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody DatasetReplaceDto dto) {
    return ApiLocaleResult.success(datasetFacade.replace(dto));
  }

  @Operation(summary = "Clone the datasets", operationId = "data:dataset:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Clone successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(datasetFacade.clone(ids));
  }

  @Operation(summary = "Import the datasets", operationId = "data:dataset:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid DatasetImportDto dto) {
    return ApiLocaleResult.success(datasetFacade.imports(dto));
  }

  @Operation(summary = "Import the inner dataset example", operationId = "data:dataset:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(datasetFacade.importExample(projectId));
  }

  @Operation(summary = "Delete datasets", operationId = "data:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Dataset ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    datasetFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of dataset", operationId = "data:dataset:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{id}")
  public ApiLocaleResult<DatasetDetailVo> detail(
      @Parameter(name = "id", description = "Dataset id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasetFacade.detail(id));
  }

  @Operation(summary = "Preview the value of dataset", operationId = "data:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @PostMapping("/value/preview")
  public ApiLocaleResult<LinkedHashMap<String, List<String>>> valuePreview(
      @RequestBody DatasetValuePreviewDto dto) {
    return ApiLocaleResult.success(datasetFacade.valuePreview(dto));
  }

  @Operation(summary = "Query the list of datasets", operationId = "data:dataset:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasetDetailVo>> list(
      @Valid @ParameterObject DatasetFindDto dto) {
    return ApiLocaleResult.success(datasetFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of datasets", operationId = "data:dataset:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<DatasetDetailVo>> search(
      @Valid @ParameterObject DatasetSearchDto dto) {
    return ApiLocaleResult.success(datasetFacade.search(dto));
  }

  @Operation(summary = "Export the datasets", operationId = "data:dataset:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject DatasetExportDto dto,
      HttpServletResponse response) {
    return datasetFacade.export(dto, response);
  }

}
