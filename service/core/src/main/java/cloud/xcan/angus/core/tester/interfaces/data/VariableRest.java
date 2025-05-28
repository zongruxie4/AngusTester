package cloud.xcan.angus.core.tester.interfaces.data;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.data.facade.VariableFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
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
import java.util.List;
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

@Tag(name = "DataVariable", description = "Test Variable Definitions - Unified interface for declaring and maintaining test variables (e.g., environment variables, global parameters).")
@Validated
@RestController
@RequestMapping("/api/v1/variable")
public class VariableRest {

  @Resource
  private VariableFacade variableFacade;

  @Operation(summary = "Add variable", operationId = "data:variable:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody VariableAddDto dto) {
    return ApiLocaleResult.success(variableFacade.add(dto));
  }

  @Operation(summary = "Update variable", operationId = "data:variable:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody VariableUpdateDto dto) {
    variableFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace variable", operationId = "data:variable:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody VariableReplaceDto dto) {
    return ApiLocaleResult.success(variableFacade.replace(dto));
  }

  @Operation(summary = "Clone the variables", operationId = "data:variable:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Clone successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(variableFacade.clone(ids));
  }

  @Operation(summary = "Import the variables", operationId = "data:variable:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object"))  @Valid VariableImportDto dto) {
    return ApiLocaleResult.success(variableFacade.imports(dto));
  }

  @Operation(summary = "Import the inner variable example", operationId = "data:variable:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(variableFacade.importExample(projectId));
  }

  @Operation(summary = "Delete variables", operationId = "data:variable:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Variable ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    variableFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of variable", operationId = "data:variable:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{id}")
  public ApiLocaleResult<VariableDetailVo> detail(
      @Parameter(name = "id", description = "Variable id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(variableFacade.detail(id));
  }

  @Operation(summary = "Preview the value of variable", operationId = "data:variable:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @PostMapping("/value/preview")
  public ApiLocaleResult<String> valuePreview(@RequestBody VariableValuePreviewDto dto) {
    return ApiLocaleResult.successData(variableFacade.valuePreview(dto));
  }

  @Operation(summary = "Query the list of variables", operationId = "data:variable:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<VariableDetailVo>> list(@Valid VariableFindDto dto) {
    return ApiLocaleResult.success(variableFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of variables", operationId = "data:variable:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<VariableDetailVo>> search(@Valid VariableSearchDto dto) {
    return ApiLocaleResult.success(variableFacade.search(dto));
  }

  @Operation(summary = "Export the variables", operationId = "data:variable:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid VariableExportDto dto, HttpServletResponse response) {
    return variableFacade.export(dto, response);
  }
}
