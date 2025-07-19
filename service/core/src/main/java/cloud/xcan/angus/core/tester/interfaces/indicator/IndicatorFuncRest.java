package cloud.xcan.angus.core.tester.interfaces.indicator;


import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorFuncFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.FuncVo;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Indicator - FunctionalTest", description = "Functional Test Indicator Management - "
    + "Configure and manage functional test types (smoke testing, security testing) and their evaluation criteria")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorFuncRest {

  @Resource
  private IndicatorFuncFacade indicatorFuncFacade;

  @Operation(summary = "Add the indicator of functionality", operationId = "indicator:func:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/func")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncAddDto dto) {
    return ApiLocaleResult.success(indicatorFuncFacade.add(dto));
  }

  @Operation(summary = "Replace the indicator of functionality", operationId = "indicator:func:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/func")
  public ApiLocaleResult<?> replace(@Valid @RequestBody FuncReplaceDto dto) {
    indicatorFuncFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the functionality indicator of target", operationId = "indicator:func:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/func")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    indicatorFuncFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(summary = "Query the indicator detail of functionality", operationId = "indicator:func:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/func")
  public ApiLocaleResult<FuncVo> detail(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorFuncFacade.detail(targetType, targetId));
  }

  @Operation(summary = "Query the indicator detail of functionality", description = "Note: Return to default configuration when not set", operationId = "indicator:func:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{targetType}/{targetId}/func/detailOrDefault")
  public ApiLocaleResult<FuncVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target Type, allowable values: API", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorFuncFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(summary = "Query the indicator list of functionality", operationId = "indicator:func:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/func")
  public ApiLocaleResult<PageResult<FuncListVo>> list(@Valid @ParameterObject FuncFindDto dto) {
    return ApiLocaleResult.success(indicatorFuncFacade.list(dto));
  }

}
