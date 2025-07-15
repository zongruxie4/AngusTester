package cloud.xcan.angus.core.tester.interfaces.data;


import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasourceFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceTestVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DataDatasource", description = "Entry point for executing JDBC-based tests and managing referenced data sources (e.g., database connections)")
@Validated
@RestController
@RequestMapping("/api/v1/data/datasource")
public class DatasourceRest {

  @Resource
  private DatasourceFacade datasourceFacade;

  @Operation(summary = "Add the datasource of data", operationId = "data:datasource:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasourceAddDto dto) {
    return ApiLocaleResult.success(datasourceFacade.add(dto));
  }

  @Operation(summary = "Replace the datasource of data", operationId = "data:datasource:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody DatasourceReplaceDto dto) {
    return ApiLocaleResult.success(datasourceFacade.replace(dto));
  }

  @Operation(summary = "Delete the datasource of data", operationId = "data:datasource:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    datasourceFacade.delete(id);
  }

  @Operation(summary = "Test the configuration of data datasource", operationId = "data:datasource:sync:test:byId")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tested successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/test")
  public ApiLocaleResult<DatasourceTestVo> testById(
      @Parameter(name = "id", description = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.testById(id));
  }

  @Operation(summary = "Test the configuration of data datasource", operationId = "data:datasource:sync:test:byParam")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tested successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/config/test")
  public ApiLocaleResult<DatasourceTestVo> testByConfig(
      @Valid @ParameterObject DatasourceTestDto dto) {
    return ApiLocaleResult.success(datasourceFacade.testByConfig(dto));
  }

  @Operation(summary = "Query the detail of data datasource", operationId = "data:datasource:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<DatasourceDetailVo> detail(
      @Parameter(name = "id", description = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.detail(id));
  }

  @Operation(summary = "Query the list of data datasource", operationId = "data:datasource:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasourceVo>> list(
      @Valid @ParameterObject DatasourceFindDto dto) {
    return ApiLocaleResult.success(datasourceFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of data datasource", operationId = "data:datasource:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<DatasourceVo>> search(
      @Valid @ParameterObject DatasourceSearchDto dto) {
    return ApiLocaleResult.success(datasourceFacade.search(dto));
  }
}
