package cloud.xcan.angus.core.tester.interfaces.data;


import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasourceFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
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

@Tag(name = "Datasource", description = "Datasource Management - APIs for managing database connections and executing JDBC-based tests with connection testing and validation capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/data/datasource")
public class DatasourceRest {

  @Resource
  private DatasourceFacade datasourceFacade;

  @Operation(summary = "Create new datasource", 
      description = "Create a new database datasource with connection configuration and validation",
      operationId = "data:datasource:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Datasource created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasourceAddDto dto) {
    return ApiLocaleResult.success(datasourceFacade.add(dto));
  }

  @Operation(summary = "Replace datasource", 
      description = "Replace existing datasource with new configuration and connection settings",
      operationId = "data:datasource:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasource replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Datasource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody DatasourceReplaceDto dto) {
    return ApiLocaleResult.success(datasourceFacade.replace(dto));
  }

  @Operation(summary = "Delete datasource", 
      description = "Remove datasource from the system with cleanup and validation",
      operationId = "data:datasource:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Datasource deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Datasource identifier for deletion", required = true) @PathVariable("id") Long id) {
    datasourceFacade.delete(id);
  }

  @Operation(summary = "Test datasource connection by ID", 
      description = "Test database connection for existing datasource with validation and error reporting",
      operationId = "data:datasource:sync:test:byId")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasource connection test completed successfully"),
      @ApiResponse(responseCode = "404", description = "Datasource not found")})
  @GetMapping(value = "/{id}/test")
  public ApiLocaleResult<DatasourceTestVo> testById(
      @Parameter(name = "id", description = "Datasource identifier for connection testing", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.testById(id));
  }

  @Operation(summary = "Test datasource connection by configuration", 
      description = "Test database connection using provided configuration parameters with real-time validation",
      operationId = "data:datasource:sync:test:byParam")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasource connection test completed successfully"),
      @ApiResponse(responseCode = "404", description = "Configuration validation failed")})
  @GetMapping(value = "/config/test")
  public ApiLocaleResult<DatasourceTestVo> testByConfig(
      @Valid @ParameterObject DatasourceTestDto dto) {
    return ApiLocaleResult.success(datasourceFacade.testByConfig(dto));
  }

  @Operation(summary = "Get datasource details", 
      description = "Retrieve datasource details including configuration, status, and metadata",
      operationId = "data:datasource:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasource details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Datasource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<DatasourceDetailVo> detail(
      @Parameter(name = "id", description = "Datasource identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.detail(id));
  }

  @Operation(summary = "Query datasource list", 
      description = "Retrieve paginated list of datasources with filtering and search options",
      operationId = "data:datasource:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Datasource list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasourceVo>> list(
      @Valid @ParameterObject DatasourceFindDto dto) {
    return ApiLocaleResult.success(datasourceFacade.list(dto));
  }

}
