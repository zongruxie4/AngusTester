package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.gm.analysis.dto.CustomizationSummaryDto;
import cloud.xcan.sdf.api.gm.analysis.vo.SummaryQueryDefinitionVo;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisCustomizationFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * It will automatically invalid after being consolidated to other package.
 */
@Api(tags = "AnalysisCustomization")
@Validated
@RestController
@RequestMapping("/api/v1/analysis/customization")
public class AnalysisCustomizationRest {

  @Resource
  private AnalysisCustomizationFacade analysisCustomizationFacade;

  @ApiOperation(value = "Resource customization analysis definition", nickname = "analysis:customization:summary:definition")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/summary/definition")
  public ApiLocaleResult<SummaryQueryDefinitionVo> definition() {
    return ApiLocaleResult.success(analysisCustomizationFacade.definitions());
  }

  @ApiOperation(value = "Resource customization analysis", nickname = "analysis:customization:summary")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/summary")
  public ApiLocaleResult<Object> summary(@Valid CustomizationSummaryDto dto) {
    return ApiLocaleResult.success(analysisCustomizationFacade.summary(dto));
  }

  @ApiOperation(value = "Batch resources customization analysis", nickname = "analysis:customization:summary:batch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/summary/batch")
  public ApiLocaleResult<Map<String, Object>> summary(
      @Valid @Size(min = 1) List<CustomizationSummaryDto> dtos) {
    return ApiLocaleResult.success(analysisCustomizationFacade.summary(dtos));
  }


}
