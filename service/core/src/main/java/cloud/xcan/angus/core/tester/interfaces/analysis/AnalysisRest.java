package cloud.xcan.angus.core.tester.interfaces.analysis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Analysis", description = "Data Analysis Management - "
    + "Provides a centralized entry point for analyzing and querying data related to development tasks and testing activities, "
    + "enabling teams to gain insights and make informed decisions")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
@Conditional(NotCommunityEditionCondition.class)
public class AnalysisRest {

  @Resource
  private AnalysisFacade analysisFacade;

  @Operation(summary = "Create analysis task", description = "Create a new data analysis task for analyzing development tasks and testing activities data", operationId = "analysis:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis task created successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody AnalysisAddDto dto) {
    return ApiLocaleResult.success(analysisFacade.add(dto));
  }

  @Operation(summary = "Update analysis task", description = "Update configuration information of an existing analysis task", operationId = "analysis:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis task updated successfully"),
      @ApiResponse(responseCode = "404", description = "Analysis task not found")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody AnalysisUpdateDto dto) {
    analysisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace analysis task", description = "Completely replace all configuration information of an analysis task", operationId = "analysis:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis task replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Analysis task not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody AnalysisReplaceDto dto) {
    return ApiLocaleResult.success(analysisFacade.replace(dto));
  }

  @Operation(summary = "Refresh analysis snapshot data", description = "Manually trigger refresh of analysis snapshot data to get latest analysis results", operationId = "analysis:refresh")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis snapshot refreshed successfully"),
      @ApiResponse(responseCode = "404", description = "Analysis task not found")
  })
  @PostMapping("/{id}/refresh")
  public ApiLocaleResult<?> refresh(
      @Parameter(name = "id", description = "Analysis task ID", required = true) @PathVariable("id") Long id) {
    analysisFacade.refresh(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete analysis tasks", description = "Batch delete specified analysis tasks", operationId = "analysis:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Analysis tasks deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "List of analysis task IDs", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    analysisFacade.delete(ids);
  }

  @Operation(summary = "Get analysis task details", description = "Retrieve detailed information of an analysis task by ID", operationId = "analysis:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis task details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Analysis task not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<AnalysisDetailVo> detail(
      @Parameter(name = "id", description = "Analysis task ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(analysisFacade.detail(id));
  }

  @Operation(summary = "List analysis tasks", description = "Retrieve paginated list of analysis tasks with filtering support", operationId = "analysis:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis tasks list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<AnalysisListVo>> list(
      @Valid @ParameterObject AnalysisFindDto dto) {
    return ApiLocaleResult.success(analysisFacade.list(dto));
  }

  @Operation(summary = "Search analysis tasks", description = "Full-text search analysis tasks based on keywords with fuzzy matching support", operationId = "analysis:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis tasks search completed successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<AnalysisListVo>> search(
      @Valid @ParameterObject AnalysisSearchDto dto) {
    return ApiLocaleResult.success(analysisFacade.search(dto));
  }

  @Operation(summary = "Export analysis overview", description = "Export overview data of a specific analysis task in multiple formats", operationId = "analysis:overview:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Analysis overview exported successfully")})
  @GetMapping(value = "/{id}/overview/export")
  public ResponseEntity<org.springframework.core.io.Resource> overviewExport(
      @Parameter(name = "id", description = "Analysis task ID", required = true) @PathVariable("id") Long id,
      HttpServletResponse response) {
    return analysisFacade.overviewExport(id, response);
  }

}
