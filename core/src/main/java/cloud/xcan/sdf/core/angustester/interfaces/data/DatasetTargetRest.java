package cloud.xcan.sdf.core.angustester.interfaces.data;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DatasetTargetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "DataDatasetTarget")
@Validated
@RestController
@RequestMapping("/api/v1")
public class DatasetTargetRest {

  @Resource
  private DatasetTargetFacade datasetTargetFacade;

  @ApiOperation(value = "Add target dataset", nickname = "data:target:dataset:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody LinkedHashSet<Long> datasetIds) {
    return ApiLocaleResult.success(datasetTargetFacade.add(targetId, targetType, datasetIds));
  }

  @ApiOperation(value = "Delete target datasets", nickname = "data:target:dataset:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/dataset")
  public void delete(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> datasetIds) {
    datasetTargetFacade.delete(targetId, targetType, datasetIds);
  }

  @ApiOperation(value = "Query the list of target datasets", nickname = "data:dataset:target:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/target/{targetId}/{targetType}/dataset")
  public ApiLocaleResult<List<DatasetDetailVo>> list(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(datasetTargetFacade.list(targetId, targetType));
  }

  @ApiOperation(value = "Query the list of dataset targets", nickname = "data:dataset:target:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/dataset/{datasetId}/target")
  public ApiLocaleResult<List<DatasetTargetVo>> listTarget(
      @ApiParam(name = "datasetId", value = "Dataset id", required = true) @PathVariable("datasetId") Long datasetId) {
    return ApiLocaleResult.success(datasetTargetFacade.listTarget(datasetId));
  }

  @ApiOperation(value = "Preview the values of target datasets", nickname = "data:target:dataset:value:preview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/target/{targetId}/{targetType}/dataset/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(datasetTargetFacade.valuePreview(targetId, targetType));
  }
}