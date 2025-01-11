package cloud.xcan.sdf.core.angustester.interfaces.indicator;


import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.FuncVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorFuncFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "IndicatorFunc")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorFuncRest {

  @Resource
  private IndicatorFuncFacade funcFacade;

  @ApiOperation(value = "Add the indicator of functionality", nickname = "indicator:func:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/func")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncAddDto dto) {
    return ApiLocaleResult.success(funcFacade.add(dto));
  }

  @ApiOperation(value = "Replace the indicator of functionality or throw 404 when it doesn't exist", nickname = "indicator:func:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/func")
  public ApiLocaleResult<?> replace(@Valid @RequestBody FuncReplaceDto dto) {
    funcFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the functionality indicator of target", nickname = "indicator:func:target:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/func")
  public void deleteByTarget(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API", allowableValues = "API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    funcFacade.deleteByTarget(targetType, targetId);
  }

  @ApiOperation(value = "Query the indicator detail of functionality", nickname = "indicator:func:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/func")
  public ApiLocaleResult<FuncVo> detail(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API", allowableValues = "API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(funcFacade.detail(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator detail of functionality, return to default configuration when not set", nickname = "indicator:func:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{targetType}/{targetId}/func/detailOrDefault")
  public ApiLocaleResult<FuncVo> detailOrDefault(
      @ApiParam(name = "targetType", value = "Target Type, allowable values: API", allowableValues = "API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @ApiParam(name = "targetId", value = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(funcFacade.detailOrDefault(targetType, targetId));
  }

  @ApiOperation(value = "Query the indicator list of functionality", nickname = "indicator:func:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/func")
  public ApiLocaleResult<PageResult<FuncListVo>> list(@Valid FuncFindDto dto) {
    return ApiLocaleResult.success(funcFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the indicator of functionality", nickname = "indicator:func:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/func/search")
  public ApiLocaleResult<PageResult<FuncListVo>> search(@Valid FuncSearchDto dto) {
    return ApiLocaleResult.success(funcFacade.search(dto));
  }

}