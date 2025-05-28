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

@Tag(name = "DataDatasetTarget", description = "Test Dataset Associations - Management interface for establishing and maintaining links between test datasets and test resources (APIs/scenarios).")
@Validated
@RestController
@RequestMapping("/api/v1")
public class DatasetTargetRest {

  @Resource
  private DatasetTargetFacade datasetTargetFacade;

  @Operation(summary = "Add target dataset", operationId = "data:target:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values: API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<Long> datasetIds) {
    return ApiLocaleResult.success(datasetTargetFacade.add(targetId, targetType, datasetIds));
  }

  @Operation(summary = "Delete target datasets", operationId = "data:target:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/dataset")
  public void delete(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> datasetIds) {
    datasetTargetFacade.delete(targetId, targetType, datasetIds);
  }

  @Operation(summary = "Query the list of target datasets", operationId = "data:dataset:target:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<DatasetDetailVo>> list(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(datasetTargetFacade.list(targetId, targetType));
  }

  @Operation(summary = "Query the list of dataset targets", operationId = "data:dataset:target:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/dataset/{datasetId}/target")
  public ApiLocaleResult<List<DatasetTargetVo>> listTarget(
      @Parameter(name = "datasetId", description = "Dataset id", required = true) @PathVariable("datasetId") Long datasetId) {
    return ApiLocaleResult.success(datasetTargetFacade.listTarget(datasetId));
  }

  @Operation(summary = "Preview the values of target datasets", operationId = "data:target:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/target/{targetId}/{targetType}/dataset/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "targetType", description = "Target type, allowable values : API, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(datasetTargetFacade.valuePreview(targetId, targetType));
  }
}
