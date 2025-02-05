package cloud.xcan.sdf.core.angustester.interfaces.data;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.VariableTargetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableTargetVo;
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

@Api(tags = "DataVariableTarget")
@Validated
@RestController
@RequestMapping("/api/v1")
public class VariableTargetRest {

  @Resource
  private VariableTargetFacade variableTargetFacade;

  @ApiOperation(value = "Add target variable", nickname = "data:target:variable:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody LinkedHashSet<Long> variableIds) {
    return ApiLocaleResult.success(variableTargetFacade.add(targetId, targetType, variableIds));
  }

  @ApiOperation(value = "Delete target variables", nickname = "data:target:variable:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/target/{targetId}/{targetType}/variable")
  public void delete(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> variableIds) {
    variableTargetFacade.delete(targetId, targetType, variableIds);
  }

  @ApiOperation(value = "Query the list of target variables", nickname = "data:target:variable:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/target/{targetId}/{targetType}/variable")
  public ApiLocaleResult<List<VariableDetailVo>> list(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.success(variableTargetFacade.list(targetId, targetType));
  }

  @ApiOperation(value = "Query the list of variable targets", nickname = "data:variable:target:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/variable/{variableId}/target")
  public ApiLocaleResult<List<VariableTargetVo>> listTarget(
      @ApiParam(name = "variableId", value = "Variable id", required = true) @PathVariable("variableId") Long variableId) {
    return ApiLocaleResult.success(variableTargetFacade.listTarget(variableId));
  }

  @ApiOperation(value = "Preview the values of target variables", nickname = "data:target:variable:value:preview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/target/{targetId}/{targetType}/variable/value/preview")
  public ApiLocaleResult<Map<String, String>> valuePreview(
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "targetType", value = "Target type", allowableValues = "API, API_CASE, SCENARIO", required = true) @PathVariable("targetType") String targetType) {
    return ApiLocaleResult.successData(variableTargetFacade.valuePreview(targetId, targetType));
  }

}