package cloud.xcan.sdf.core.angustester.interfaces.data;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DatasetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "DataDataset")
@Validated
@RestController
@RequestMapping("/api/v1/dataset")
public class DatasetRest {

  @Resource
  private DatasetFacade datasetFacade;

  @ApiOperation(value = "Add dataset", nickname = "data:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasetAddDto dto) {
    return ApiLocaleResult.success(datasetFacade.add(dto));
  }

  @ApiOperation(value = "Update dataset", nickname = "data:dataset:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody DatasetUpdateDto dto) {
    datasetFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace dataset", nickname = "data:dataset:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody DatasetReplaceDto dto) {
    return ApiLocaleResult.success(datasetFacade.replace(dto));
  }

  @ApiOperation(value = "Clone the datasets", nickname = "data:dataset:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Clone successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(datasetFacade.clone(ids));
  }

  @ApiOperation(value = "Import the datasets", nickname = "data:dataset:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(@Valid DatasetImportDto dto) {
    return ApiLocaleResult.success(datasetFacade.imports(dto));
  }

  @ApiOperation(value = "Import the inner dataset example", nickname = "data:dataset:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> exampleImport(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(datasetFacade.exampleImport(projectId));
  }

  @ApiOperation(value = "Delete datasets", nickname = "data:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Dataset ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    datasetFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of dataset", nickname = "data:dataset:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{id}")
  public ApiLocaleResult<DatasetDetailVo> detail(
      @ApiParam(name = "id", value = "Dataset id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasetFacade.detail(id));
  }

  @ApiOperation(value = "Preview the value of dataset", nickname = "data:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @PostMapping("/value/preview")
  public ApiLocaleResult<LinkedHashMap<String, List<String>>> valuePreview(
      @RequestBody DatasetValuePreviewDto dto) {
    return ApiLocaleResult.success(datasetFacade.valuePreview(dto));
  }

  @ApiOperation(value = "Query the list of datasets", nickname = "data:dataset:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasetDetailVo>> list(@Valid DatasetFindDto dto) {
    return ApiLocaleResult.success(datasetFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of datasets", nickname = "data:dataset:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<DatasetDetailVo>> search(@Valid DatasetSearchDto dto) {
    return ApiLocaleResult.success(datasetFacade.search(dto));
  }

  @ApiOperation(value = "Export the datasets", nickname = "data:dataset:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(@Valid DatasetExportDto dto,
      HttpServletResponse response) {
    return datasetFacade.export(dto, response);
  }
}
