package cloud.xcan.angus.core.tester.interfaces.mock;

import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler.assembleAllowImportSampleStatus;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisListVo;
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
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
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

@Tag(name = "Mock Apis", description = "Mock API Management - Unified management portal for creating, simulating, and organizing mock APIs with full lifecycle management capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/mock/apis")
public class MockApisRest {

  @Resource
  private MockApisFacade mockApisFacade;

  @Operation(summary = "Create mock APIs",
      description = "Create new mock API definitions with configuration for API simulation and testing",
      operationId = "mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock APIs created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisAddDto> dto) {
    return ApiLocaleResult.success(mockApisFacade.add(dto));
  }

  @Operation(summary = "Update mock APIs",
      description = "Update existing mock API configurations with partial modifications for flexible management",
      operationId = "mock:apis:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock APIs updated successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisUpdateDto> dto) {
    mockApisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace mock APIs",
      description = "Replace existing mock API configurations with new definitions for updates",
      operationId = "mock:apis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock APIs replaced successfully")
  })
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisReplaceDto> dto) {
    return ApiLocaleResult.success(mockApisFacade.replace(dto));
  }

  @Operation(summary = "Clone mock API",
      description = "Create a duplicate copy of existing mock API with all configurations for rapid replication",
      operationId = "mock:apis:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock API cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Mock API identifier for cloning operation", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.clone(id));
  }

  @Operation(summary = "Move mock APIs to another service",
      description = "Transfer mock APIs from current service to target mock service for organizational restructuring",
      operationId = "mock:apis:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock APIs moved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API or target service not found")})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @Parameter(name = "targetServiceId", description = "Target mock service identifier for API transfer", required = true) @RequestParam("targetServiceId") Long targetServiceId) {
    mockApisFacade.move(ids, targetServiceId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Synchronize mock API to service instance",
      description = "Deploy mock API configuration to mock service instance for runtime availability",
      operationId = "mock:apis:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API synchronized to instance successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @Parameter(name = "id", description = "Mock API identifier for instance synchronization", required = true) @PathVariable("id") Long id) {
    mockApisFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Generate mock API from copied API",
      description = "Create new mock API based on existing API definition for rapid mock service setup",
      operationId = "mock:apis:copy:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Mock API generated from copied API successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/copy/{apisId}/{mockServiceId}")
  public ApiLocaleResult<IdKey<Long, Object>> copyApisAdd(
      @Parameter(name = "mockServiceId", description = "Mock service identifier for API generation", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "apisId", description = "Source API identifier for mock generation", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.copyApisAdd(mockServiceId, apisId));
  }

  @Operation(summary = "Generate associated mock API from API",
      description = "Create mock API with association to existing API for integrated testing workflows",
      operationId = "mock:apis:association:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Associated mock API generated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/{mockServiceId}/{apisId}")
  public ApiLocaleResult<IdKey<Long, Object>> assocApisAdd(
      @Parameter(name = "mockServiceId", description = "Mock service identifier for association", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "apisId", description = "API identifier for mock association", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.assocApisAdd(mockServiceId, apisId));
  }

  @Operation(summary = "Associate mock API with API",
      description = "Establish association between existing mock API and API for integrated testing workflows",
      operationId = "mock:apis:association:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API association updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/association/{mockApisId}/{apisId}")
  public ApiLocaleResult<?> assocApisUpdate(
      @Parameter(name = "mockApisId", description = "Mock API identifier for association", required = true) @PathVariable("mockApisId") Long mockApisId,
      @Parameter(name = "apisId", description = "API identifier for association", required = true) @PathVariable("apisId") Long apisId) {
    mockApisFacade.assocApisUpdate(mockApisId, apisId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete mock API associations",
      description = "Remove associations between mock APIs and service APIs for relationship management",
      operationId = "mock:apis:association:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Mock API associations deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/association")
  public void assocDelete(@Parameter(name = "ids", description = "Mock API identifiers for association deletion", required = true)
  @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.assocDelete(ids);
  }

  @Operation(summary = "Delete mock APIs",
      description = "Remove mock API definitions from system with cleanup",
      operationId = "mock:apis:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Mock APIs deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Mock API identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.delete(ids);
  }

  @Operation(summary = "Query mock API details",
      description = "Retrieve information about specific mock API including configuration and status",
      operationId = "mock:apis:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock API details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Mock API not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockApisDetailVo> detail(
      @Parameter(name = "id", description = "Mock API identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.detail(id));
  }

  @Operation(summary = "Query mock APIs with filtering",
      description = "Retrieve paginated list of mock APIs with filtering and search capabilities",
      operationId = "mock:apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock APIs retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<MockApisListVo>> list(
      @Valid @ParameterObject MockApisFindDto dto) {
    PageResult<MockApisListVo> result = mockApisFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }
}
