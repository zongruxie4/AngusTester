package cloud.xcan.angus.core.tester.interfaces.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.cases.ApisCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.cases.ApisCaseListVo;
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.validator.constraints.Length;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
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

@Tag(name = "APIs Test Use Cases", description = "APIs Test Use Cases Management - Comprehensive APIs for storing and managing interface testing cases, assertion rules, and test scenario configuration with lifecycle management")
@Validated
@RestController
@RequestMapping("/api/v1/apis/case")
public class ApisUseCasesRest {

  @Resource
  private ApisCaseFacade apisCaseFacade;

  @Operation(summary = "Create API functional test cases", 
      description = "Create new API functional test cases with comprehensive configuration and validation",
      operationId = "apis:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API functional test cases created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseAddDto> dto) {
    return ApiLocaleResult.success(apisCaseFacade.add(dto));
  }

  @Operation(summary = "Update API functional test cases", 
      description = "Update existing API functional test cases with comprehensive configuration and validation",
      operationId = "apis:case:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test cases updated successfully"),
      @ApiResponse(responseCode = "404", description = "One or more test cases not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseUpdateDto> dto) {
    apisCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace API functional test cases", 
      description = "Replace existing API functional test cases with complete new configuration and validation",
      operationId = "apis:case:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test cases replaced successfully")})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseReplaceDto> dto) {
    apisCaseFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Rename API functional test case", 
      description = "Update API functional test case name with comprehensive validation and metadata management",
      operationId = "apis:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test case name updated successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Test case identifier for name update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New test case name for identification", required = true) @Valid @Length(max = MAX_NAME_LENGTH_X4) @RequestParam("name") String name) {
    apisCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable functional test cases", 
      description = "Toggle functional test case status with comprehensive lifecycle management and validation",
      operationId = "func:case:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test case status updated successfully"),
      @ApiResponse(responseCode = "404", description = "One or more test cases not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @Parameter(name = "ids", description = "Test case identifiers for status update", required = true) @RequestParam(value = "ids") Set<Long> ids,
      @Valid @NotNull @Parameter(name = "enabled", description = "Test case status flag for enable/disable control", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisCaseFacade.enabled(ids, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Sync functional test cases to script", 
      description = "Force synchronize functional test cases to script with comprehensive validation and automation",
      operationId = "func:case:script:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases synchronized to script successfully"),
      @ApiResponse(responseCode = "404", description = "API not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/syncToScript")
  public ApiLocaleResult<?> syncToScript(
      @Parameter(name = "apisId", description = "API identifier for test case synchronization", required = true) @RequestParam(value = "apisId") Long apisId) {
    apisCaseFacade.syncToScript(apisId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone API functional test cases", 
      description = "Create copies of API functional test cases with all configuration and metadata",
      operationId = "apis:case:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test cases cloned successfully"),
      @ApiResponse(responseCode = "404", description = "One or more test cases not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(apisCaseFacade.clone(ids));
  }

  @Operation(summary = "Delete API functional test cases", 
      description = "Remove API functional test cases from the system with batch operation support and comprehensive cleanup",
      operationId = "apis:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API functional test cases deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    apisCaseFacade.delete(ids);
  }

  @Operation(summary = "Get API functional test case details", 
      description = "Retrieve comprehensive API functional test case details including configuration and metadata",
      operationId = "apis:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test case details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisCaseDetailVo> detail(
      @Valid @Parameter(name = "id", description = "Test case identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisCaseFacade.detail(id));
  }

  @Operation(summary = "Query API functional test cases list", 
      description = "Retrieve paginated list of API functional test cases with comprehensive filtering and search options",
      operationId = "apis:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API functional test cases list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisCaseListVo>> list(
      @Valid @ParameterObject ApisCaseFindDto dto) {
    return ApiLocaleResult.success(apisCaseFacade.list(dto));
  }

}
