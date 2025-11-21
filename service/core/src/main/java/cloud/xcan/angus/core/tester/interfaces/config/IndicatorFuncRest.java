package cloud.xcan.angus.core.tester.interfaces.config;


import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.config.facade.IndicatorFuncFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.FuncListVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.FuncVo;
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

@Tag(name = "Indicator - Functional Test", description = "Functional Test Indicator Management - APIs for configuring and managing functional test types (smoke testing, security testing) and their evaluation criteria with target-based configuration")
@Validated
@RestController
@RequestMapping("/api/v1/indicator")
public class IndicatorFuncRest {

  @Resource
  private IndicatorFuncFacade indicatorFuncFacade;

  @Operation(summary = "Create functional test indicator",
      description = "Create a new functional test indicator with configuration for test type evaluation",
      operationId = "indicator:func:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test indicator created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/func")
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncAddDto dto) {
    return ApiLocaleResult.success(indicatorFuncFacade.add(dto));
  }

  @Operation(summary = "Replace functional test indicator",
      description = "Replace an existing functional test indicator with new configuration",
      operationId = "indicator:func:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test indicator replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test indicator not found")
  })
  @PutMapping("/func")
  public ApiLocaleResult<?> replace(@Valid @RequestBody FuncReplaceDto dto) {
    indicatorFuncFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete functional test indicator by target",
      description = "Remove functional test indicator configuration for a specific target type and identifier",
      operationId = "indicator:func:target:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Functional test indicator deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{targetType}/{targetId}/func")
  public void deleteByTarget(
      @Parameter(name = "targetType", description = "Target type for indicator configuration (API)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator configuration", required = true) @PathVariable("targetId") Long targetId) {
    indicatorFuncFacade.deleteByTarget(targetType, targetId);
  }

  @Operation(summary = "Get functional test indicator details",
      description = "Retrieve details of functional test indicator for a specific target",
      operationId = "indicator:func:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/func")
  public ApiLocaleResult<FuncVo> detail(
      @Parameter(name = "targetType", description = "Target type for indicator query (API)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorFuncFacade.detail(targetType, targetId));
  }

  @Operation(summary = "Get functional test indicator details or default",
      description = "Retrieve functional test indicator details for a specific target, returning default configuration when not set",
      operationId = "indicator:func:audit:detailOrDefault")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test indicator details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test indicator not found")})
  @GetMapping(value = "/{targetType}/{targetId}/func/detailOrDefault")
  public ApiLocaleResult<FuncVo> detailOrDefault(
      @Parameter(name = "targetType", description = "Target type for indicator query (API)", required = true)
      @PathVariable("targetType") CombinedTargetType targetType,
      @Parameter(name = "targetId", description = "Target identifier for indicator query", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(indicatorFuncFacade.detailOrDefault(targetType, targetId));
  }

  @Operation(summary = "List functional test indicators",
      description = "Retrieve paginated list of functional test indicators with filtering and search options",
      operationId = "indicator:func:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test indicator list retrieved successfully")})
  @GetMapping("/func")
  public ApiLocaleResult<PageResult<FuncListVo>> list(@Valid @ParameterObject FuncFindDto dto) {
    return ApiLocaleResult.success(indicatorFuncFacade.list(dto));
  }

}
