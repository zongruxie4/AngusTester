package cloud.xcan.sdf.core.angustester.interfaces.data;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.VariableFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableValuePreviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
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

@Api(tags = "DataVariable")
@Validated
@RestController
@RequestMapping("/api/v1/variable")
public class VariableRest {

  @Resource
  private VariableFacade variableFacade;

  @ApiOperation(value = "Add variable", nickname = "data:variable:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody VariableAddDto dto) {
    return ApiLocaleResult.success(variableFacade.add(dto));
  }

  @ApiOperation(value = "Update variable", nickname = "data:variable:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody VariableUpdateDto dto) {
    variableFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace variable", nickname = "data:variable:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody VariableReplaceDto dto) {
    return ApiLocaleResult.success(variableFacade.replace(dto));
  }

  @ApiOperation(value = "Clone the variables", nickname = "data:variable:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Clone successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(variableFacade.clone(ids));
  }

  @ApiOperation(value = "Delete variables", nickname = "data:variable:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Variable ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    variableFacade.delete(ids);
  }

  @ApiOperation(value = "Import the variables", nickname = "data:variable:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(@Valid VariableImportDto dto) {
    return ApiLocaleResult.success(variableFacade.imports(dto));
  }

  @ApiOperation(value = "Export the variables", nickname = "data:variable:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid VariableExportDto dto, HttpServletResponse response) {
    return variableFacade.export(dto, response);
  }

  @ApiOperation(value = "Query the detail of variable", nickname = "data:variable:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{id}")
  public ApiLocaleResult<VariableDetailVo> detail(
      @ApiParam(name = "id", value = "Variable id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(variableFacade.detail(id));
  }

  @ApiOperation(value = "Preview the value of variable", nickname = "data:variable:value:preview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @PostMapping("/value/preview")
  public ApiLocaleResult<String> valuePreview(@RequestBody VariableValuePreviewDto dto) {
    return ApiLocaleResult.successData(variableFacade.valuePreview(dto));
  }

  @ApiOperation(value = "Query the list of variables", nickname = "data:variable:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<VariableDetailVo>> list(@Valid VariableFindDto dto) {
    return ApiLocaleResult.success(variableFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of variables", nickname = "data:variable:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<VariableDetailVo>> search(@Valid VariableSearchDto dto) {
    return ApiLocaleResult.success(variableFacade.search(dto));
  }
}