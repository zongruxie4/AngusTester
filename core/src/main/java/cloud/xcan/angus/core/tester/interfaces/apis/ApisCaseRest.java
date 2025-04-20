package cloud.xcan.angus.core.tester.interfaces.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseSearchDto;
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

@Tag(name = "ApisCase", description = "API Test Case Management - For storing and managing interface testing cases and assertion rules.")
@Validated
@RestController
@RequestMapping("/api/v1/apis/case")
public class ApisCaseRest {

  @Resource
  private ApisCaseFacade apisCaseFacade;

  @Operation(description = "Add apis functional test cases", operationId = "apis:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseAddDto> dto) {
    return ApiLocaleResult.success(apisCaseFacade.add(dto));
  }

  @Operation(description = "Update apis functional test cases", operationId = "apis:case:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseUpdateDto> dto) {
    apisCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace apis functional test cases", operationId = "apis:case:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisCaseReplaceDto> dto) {
    apisCaseFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace the name of apis functional test cases", operationId = "apis:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New case name", required = true) @Valid @Length(max = MAX_NAME_LENGTH_X4) @RequestParam("name") String name) {
    apisCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Enable or disable the functional test cases", operationId = "func:case:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @Parameter(name = "ids", description = "Case ids", required = true) @RequestParam(value = "ids") Set<Long> ids,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisCaseFacade.enabled(ids, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Force synchronize the functional test cases to script", operationId = "func:case:script:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Synchronized successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/syncToScript")
  public ApiLocaleResult<?> syncToScript(
      @Parameter(name = "apisId", description = "Apis id", required = true) @RequestParam(value = "apisId") Long apisId) {
    apisCaseFacade.syncToScript(apisId);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Clone the apis functional test cases", operationId = "apis:case:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Clone successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(apisCaseFacade.clone(ids));
  }

  @Operation(description = "Delete apis functional test cases", operationId = "apis:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    apisCaseFacade.delete(ids);
  }

  @Operation(description = "Query the detail of apis functional test cases", operationId = "apis:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisCaseDetailVo> detail(
      @Valid @Parameter(name = "id", description = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisCaseFacade.detail(id));
  }

  @Operation(description = "Query the list of apis functional test cases", operationId = "apis:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisCaseListVo>> list(@Valid ApisCaseFindDto dto) {
    return ApiLocaleResult.success(apisCaseFacade.list(dto));
  }

  @Operation(description = "Fulltext search the list of apis functional test cases", operationId = "apis:case:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisCaseListVo>> search(@Valid ApisCaseSearchDto dto) {
    return ApiLocaleResult.success(apisCaseFacade.search(dto));
  }

}
