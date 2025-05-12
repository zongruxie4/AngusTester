package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncBaselineFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
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

@Tag(name = "FuncBaseline", description = "Functional Test Baseline Management - Defining and recording standard/reference attributes of functional test cases (e.g., pass/fail criteria, priority levels).")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineRest {

  @Resource
  private FuncBaselineFacade funcBaselineFacade;

  @Operation(description = "Add the baseline", operationId = "func:baseline:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncBaselineAddDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.add(dto));
  }

  @Operation(description = "Update the baseline", operationId = "func:baseline:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncBaselineUpdateDto dto) {
    funcBaselineFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace the baseline", operationId = "func:baseline:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncBaselineReplaceDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.replace(dto));
  }

  @Operation(description = "Establish the baseline", operationId = "func:baseline:establish")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/establish")
  public ApiLocaleResult<?> establish(
      @Parameter(name = "id", description = "Baseline id", required = true) @PathVariable("id") Long id) {
    funcBaselineFacade.establish(id);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the baselines", operationId = "func:baseline:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Case baseline ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcBaselineFacade.delete(ids);
  }

  @Operation(description = "Query the detail of baseline", operationId = "func:baseline:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncBaselineDetailVo> detail(
      @Parameter(name = "id", description = "Baseline id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcBaselineFacade.detail(id));
  }

  @Operation(description = "Query the list of baseline", operationId = "func:baseline:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncBaselineVo>> list(
      @Valid @ParameterObject FuncBaselineFindDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.list(dto));
  }

  @Operation(description = "Fulltext search the list of baseline", operationId = "func:baseline:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncBaselineVo>> search(
      @Valid @ParameterObject FuncBaselineSearchDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.search(dto));
  }

}
