package cloud.xcan.angus.core.tester.interfaces.analysis;


import cloud.xcan.angus.api.gm.analysis.dto.CustomizationSummaryDto;
import cloud.xcan.angus.api.gm.analysis.vo.SummaryQueryDefinitionVo;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisCustomizationFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customized analysis definition and statistical implementation APIs.
 * <p>
 * Provides comprehensive support for querying analysis resource definitions and retrieving customized statistical information.
 * <p>
 * This component will be automatically deprecated after consolidation into other packages.
 */
@Tag(name = "Analysis Customization", description = 
    "Customized Analysis Framework - Advanced analytics implementation APIs for querying supported analysis resource definitions and retrieving customized statistical information with flexible configuration options")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/customization")
public class AnalysisCustomizationRest {

  @Resource
  private AnalysisCustomizationFacade analysisCustomizationFacade;

  @Operation(summary = "Customized analysis definition query", 
      description = "Retrieve comprehensive definition information for supported analysis resources and query configurations",
      operationId = "analysis:customization:summary:definition")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analysis definition information retrieved successfully")})
  @GetMapping("/summary/definition")
  public ApiLocaleResult<SummaryQueryDefinitionVo> definition() {
    return ApiLocaleResult.success(analysisCustomizationFacade.definitions());
  }

  @Operation(summary = "Customized resource analysis", 
      description = "Perform customized analysis on specific resources with flexible query parameters and statistical aggregation",
      operationId = "analysis:customization:summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Customized analysis results retrieved successfully")})
  @GetMapping("/summary")
  public ApiLocaleResult<Object> summary(
      @Valid @ParameterObject CustomizationSummaryDto dto) {
    return ApiLocaleResult.success(analysisCustomizationFacade.summary(dto));
  }

  @Operation(summary = "Batch customized resource analysis", 
      description = "Perform batch analysis on multiple resources with comprehensive statistical aggregation and result consolidation",
      operationId = "analysis:customization:summary:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Batch analysis results retrieved successfully")})
  @GetMapping("/summary/batch")
  public ApiLocaleResult<Map<String, Object>> summary(
      @Valid @ParameterObject List<CustomizationSummaryDto> dto) {
    return ApiLocaleResult.success(analysisCustomizationFacade.summary(dto));
  }


}
