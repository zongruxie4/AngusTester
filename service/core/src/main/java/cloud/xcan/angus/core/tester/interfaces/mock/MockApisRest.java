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

@Tag(name = "MockApis", description = "Mock API Management - Unified management portal for creating, simulating, and organizing mock APIs")
@Validated
@RestController
@RequestMapping("/api/v1/mock/apis")
public class MockApisRest {

  @Resource
  private MockApisFacade mockApisFacade;

  @Operation(summary = "Add mock apis", operationId = "mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisAddDto> dto) {
    return ApiLocaleResult.success(mockApisFacade.add(dto));
  }

  @Operation(summary = "Update mock apis", operationId = "mock:apis:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisUpdateDto> dto) {
    mockApisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace mock apis", operationId = "mock:apis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")
  })
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<MockApisReplaceDto> dto) {
    return ApiLocaleResult.success(mockApisFacade.replace(dto));
  }

  @Operation(summary = "Clone mock apis", operationId = "mock:apis:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @Parameter(name = "id", description = "Mock apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.clone(id));
  }

  @Operation(summary = "Move the apis to another mock service", operationId = "mock:apis:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Moved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @Parameter(name = "targetServiceId", description = "Target mock service id", required = true) @RequestParam("targetServiceId") Long targetServiceId) {
    mockApisFacade.move(ids, targetServiceId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Synchronize the apis to mock service instance", operationId = "mock:apis:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Synchronized successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @Parameter(name = "id", description = "Mock apis id", required = true) @PathVariable("id") Long id) {
    mockApisFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Generate mock apis from copied apis", operationId = "mock:apis:copy:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/copy/{apisId}/{mockServiceId}")
  public ApiLocaleResult<IdKey<Long, Object>> copyApisAdd(
      @Parameter(name = "mockServiceId", description = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "apisId", description = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.copyApisAdd(mockServiceId, apisId));
  }

  @Operation(summary = "Generate associated mock apis from apis", operationId = "mock:apis:association:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/{mockServiceId}/{apisId}")
  public ApiLocaleResult<IdKey<Long, Object>> assocApisAdd(
      @Parameter(name = "mockServiceId", description = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @Parameter(name = "apisId", description = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.assocApisAdd(mockServiceId, apisId));
  }

  @Operation(summary = "Associate mock apis and apis", operationId = "mock:apis:association:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/association/{mockApisId}/{apisId}")
  public ApiLocaleResult<?> assocApisUpdate(
      @Parameter(name = "mockApisId", description = "Mock apis id", required = true) @PathVariable("mockApisId") Long mockApisId,
      @Parameter(name = "apisId", description = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    mockApisFacade.assocApisUpdate(mockApisId, apisId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the association between mock apis and service apis", operationId = "mock:apis:association:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/association")
  public void assocDelete(@Parameter(name = "ids", description = "Mock apis ids", required = true)
  @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.assocDelete(ids);
  }

  @Operation(summary = "Delete mock apis", operationId = "mock:apis:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Mock apis ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of mock apis", operationId = "mock:apis:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockApisDetailVo> detail(
      @Parameter(name = "id", description = "Mock apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.detail(id));
  }

  @Operation(summary = "Query the list of mock apis", operationId = "mock:apis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<MockApisListVo>> list(
      @Valid @ParameterObject MockApisFindDto dto) {
    PageResult<MockApisListVo> result = mockApisFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }
}
