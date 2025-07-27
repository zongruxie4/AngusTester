package cloud.xcan.angus.core.tester.interfaces.data;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasetTargetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
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

@Tag(name = "Dataset Target", description = "Dataset Target Management - Comprehensive APIs for establishing and maintaining associations between test datasets and test resources including APIs and scenarios with value preview capabilities")
@Validated
@RestController
@RequestMapping("/api/v1")
public class DatasetTargetRest {

  @Resource
  private DatasetTargetFacade datasetTargetFacade;

  @Operation(summary = "Associate datasets with target", 
      description = "Establish associations between test datasets and specific test resources with batch operation support",
      operationId = "data:target:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Dataset associations created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Parameter(name = "targetId", description = "Target resource identifier for dataset association", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for dataset categorization, allowable values: API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> datasetIds) {
    return ApiLocaleResult.success(datasetTargetFacade.add(targetId, targetType, datasetIds));
  }

  @Operation(summary = "Remove dataset associations", 
      description = "Remove associations between test datasets and specific test resources with batch operation support",
      operationId = "data:target:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Dataset associations removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/dataset")
  public void delete(
      @Parameter(name = "targetId", description = "Target resource identifier for dataset association removal", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for dataset categorization, allowable values: API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> datasetIds) {
    datasetTargetFacade.delete(targetId, targetType, datasetIds);
  }

  @Operation(summary = "Get target dataset associations", 
      description = "Retrieve all dataset associations for a specific test resource with comprehensive details",
      operationId = "data:target:dataset:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Target dataset associations retrieved successfully")})
  @GetMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<DatasetDetailVo>> list(
      @Parameter(name = "targetId", description = "Target resource identifier for dataset association query", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for dataset categorization, allowable values: API, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(datasetTargetFacade.list(targetId, targetType));
  }

  @Operation(summary = "Get dataset target associations", 
      description = "Retrieve all target associations for a specific dataset with comprehensive resource mapping",
      operationId = "data:dataset:target:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dataset target associations retrieved successfully")})
  @GetMapping(value = "/dataset/{datasetId}/target")
  public ApiLocaleResult<List<DatasetTargetVo>> listTarget(
      @Parameter(name = "datasetId", description = "Dataset identifier for target association query", required = true) @PathVariable("datasetId") Long datasetId) {
    return ApiLocaleResult.success(datasetTargetFacade.listTarget(datasetId));
  }

  @Operation(summary = "Preview target dataset values", 
      description = "Preview dataset parameter values for a specific test resource with dynamic processing and validation",
      operationId = "data:target:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Target dataset values preview retrieved successfully")})
  @GetMapping("/target/{targetId}/{targetType}/dataset/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @Parameter(name = "targetId", description = "Target resource identifier for dataset value preview", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target resource type for dataset categorization, allowable values: API, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(datasetTargetFacade.valuePreview(targetId, targetType));
  }
}
