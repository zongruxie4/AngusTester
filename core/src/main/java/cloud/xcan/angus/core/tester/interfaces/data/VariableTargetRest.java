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

@Tag(name = "DataVariableTarget", description = "Test Variable Bindings - Management interface for mapping defined variables to specific test resources (APIs/scenarios).")
@Validated
@RestController
@RequestMapping("/api/v1")
public class VariableTargetRest {

  @Resource
  private VariableTargetFacade variableTargetFacade;

  @Operation(description = "Add target variable", operationId = "data:target:variable:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, API_CASE, SCENARIO",  required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> variableIds) {
    return ApiLocaleResult.success(variableTargetFacade.add(targetId, targetType, variableIds));
  }

  @Operation(description = "Delete target variables", operationId = "data:target:variable:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/variable")
  public void delete(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> variableIds) {
    variableTargetFacade.delete(targetId, targetType, variableIds);
  }

  @Operation(description = "Query the list of target variables", operationId = "data:target:variable:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<VariableDetailVo>> list(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(variableTargetFacade.list(targetId, targetType));
  }

  @Operation(description = "Query the list of variable targets", operationId = "data:variable:target:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/variable/{variableId}/target")
  public ApiLocaleResult<List<VariableTargetVo>> listTarget(
      @Parameter(name = "variableId", description = "Variable id", required = true) @PathVariable("variableId") Long variableId) {
    return ApiLocaleResult.success(variableTargetFacade.listTarget(variableId));
  }

  @Operation(description = "Preview the values of target variables", operationId = "data:target:variable:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/target/{targetId}/{targetType}/variable/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(variableTargetFacade.valuePreview(targetId, targetType));
  }

}
