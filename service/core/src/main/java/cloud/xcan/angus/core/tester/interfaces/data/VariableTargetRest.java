package cloud.xcan.angus.core.tester.interfaces.data;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.data.facade.VariableTargetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableTargetVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Variable Target", description = "Variable Target Management - APIs for establishing and maintaining associations between test variables and test resources including APIs, API cases, and scenarios with value preview capabilities")
@Validated
@RestController
@RequestMapping("/api/v1")
public class VariableTargetRest {

  @Resource
  private VariableTargetFacade variableTargetFacade;

  @Operation(summary = "Associate variables with target",
      description = "Establish associations between test variables and specific test resources with batch operation support",
      operationId = "data:target:variable:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Variable associations created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Parameter(name = "targetId", description = "Target resource identifier for variable association", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for variable categorization, allowable values: API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> variableIds) {
    return ApiLocaleResult.success(variableTargetFacade.add(targetId, targetType, variableIds));
  }

  @Operation(summary = "Remove variable associations",
      description = "Remove associations between test variables and specific test resources with batch operation support",
      operationId = "data:target:variable:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Variable associations removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/variable")
  public void delete(
      @Parameter(name = "targetId", description = "Target resource identifier for variable association removal", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for variable categorization, allowable values: API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> variableIds) {
    variableTargetFacade.delete(targetId, targetType, variableIds);
  }

  @Operation(summary = "Get target variable associations",
      description = "Retrieve all variable associations for a specific test resource with comprehensive details",
      operationId = "data:target:variable:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Target variable associations retrieved successfully")})
  @GetMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<VariableDetailVo>> list(
      @Parameter(name = "targetId", description = "Target resource identifier for variable association query", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for variable categorization, allowable values: API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(variableTargetFacade.list(targetId, targetType));
  }

  @Operation(summary = "Get variable target associations",
      description = "Retrieve all target associations for a specific variable with comprehensive resource mapping",
      operationId = "data:variable:target:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Variable target associations retrieved successfully")})
  @GetMapping(value = "/variable/{variableId}/target")
  public ApiLocaleResult<List<VariableTargetVo>> listTarget(
      @Parameter(name = "variableId", description = "Variable identifier for target association query", required = true) @PathVariable("variableId") Long variableId) {
    return ApiLocaleResult.success(variableTargetFacade.listTarget(variableId));
  }

  @Operation(summary = "Preview target variable values",
      description = "Preview variable values for a specific test resource with dynamic processing and validation",
      operationId = "data:target:variable:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Target variable values preview retrieved successfully")})
  @GetMapping("/target/{targetId}/{targetType}/variable/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @Parameter(name = "targetId", description = "Target resource identifier for variable value preview", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for variable categorization, allowable values: API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(variableTargetFacade.valuePreview(targetId, targetType));
  }

}
