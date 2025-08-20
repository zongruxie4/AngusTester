package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncBaselineFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
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

@Tag(name = "Functional Test Baseline", description = "Functional Test Baseline Management - APIs for defining and recording standard/reference attributes of functional test cases with version control and establishment capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineRest {

  @Resource
  private FuncBaselineFacade funcBaselineFacade;

  @Operation(summary = "Create functional test baseline", 
      description = "Create a new functional test baseline with configuration for standard/reference attributes",
      operationId = "func:baseline:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test baseline created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncBaselineAddDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.add(dto));
  }

  @Operation(summary = "Update functional test baseline", 
      description = "Update an existing functional test baseline with partial modification support",
      operationId = "func:baseline:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test baseline updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncBaselineUpdateDto dto) {
    funcBaselineFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace functional test baseline", 
      description = "Replace an existing functional test baseline with new configuration",
      operationId = "func:baseline:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test baseline replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncBaselineReplaceDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.replace(dto));
  }

  @Operation(summary = "Establish functional test baseline", 
      description = "Establish a functional test baseline as the current reference standard for test case evaluation",
      operationId = "func:baseline:establish")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test baseline established successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/establish")
  public ApiLocaleResult<?> establish(
      @Parameter(name = "id", description = "Baseline identifier for establishment", required = true) @PathVariable("id") Long id) {
    funcBaselineFacade.establish(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete functional test baselines", 
      description = "Delete multiple functional test baselines with batch operation support",
      operationId = "func:baseline:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Functional test baselines deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Baseline identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcBaselineFacade.delete(ids);
  }

  @Operation(summary = "Get functional test baseline details", 
      description = "Retrieve details of a specific functional test baseline for analysis and review",
      operationId = "func:baseline:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test baseline details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test baseline not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncBaselineDetailVo> detail(
      @Parameter(name = "id", description = "Baseline identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcBaselineFacade.detail(id));
  }

  @Operation(summary = "List functional test baselines", 
      description = "Retrieve paginated list of functional test baselines with filtering and search options",
      operationId = "func:baseline:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test baseline list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncBaselineVo>> list(
      @Valid @ParameterObject FuncBaselineFindDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.list(dto));
  }

}
