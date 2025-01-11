package cloud.xcan.sdf.core.angustester.interfaces.analysis;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "Analysis")
@Validated
@RestController
@RequestMapping("/api/v1/analysis")
@Conditional(NotPrivateEditionCondition.class)
public class AnalysisRest {

  @Resource
  private AnalysisFacade analysisFacade;

  @ApiOperation(value = "Create analysis", nickname = "analysis:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody AnalysisAddDto dto) {
    return ApiLocaleResult.success(analysisFacade.add(dto));
  }

  @ApiOperation(value = "Update analysis", nickname = "analysis:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody AnalysisUpdateDto dto) {
    analysisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace analysis", nickname = "analysis:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody AnalysisReplaceDto dto) {
    return ApiLocaleResult.success(analysisFacade.replace(dto));
  }

  @ApiOperation(value = "Refresh the latest analysis snapshot data", nickname = "analysis:refresh")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Generated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/{id}/refresh")
  public ApiLocaleResult<?> refresh(
      @ApiParam(name = "id", value = "Analysis id", required = true) @PathVariable("id") Long id) {
    analysisFacade.refresh(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete analysis", nickname = "analysis:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Analysis ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    analysisFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of analysis", nickname = "analysis:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<AnalysisDetailVo> detail(
      @ApiParam(name = "id", value = "Analysis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(analysisFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of analysis", nickname = "analysis:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<AnalysisListVo>> list(@Valid AnalysisFindDto dto) {
    return ApiLocaleResult.success(analysisFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of analysis", nickname = "analysis:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<AnalysisListVo>> search(@Valid AnalysisSearchDto dto) {
    return ApiLocaleResult.success(analysisFacade.search(dto));
  }

  @ApiOperation(value = "Export the overview data of analysis", nickname = "analysis:overview:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/overview/export")
  public ResponseEntity<org.springframework.core.io.Resource> overviewExport(
      @ApiParam(name = "id", value = "Analysis id", required = true) @PathVariable("id") Long id,
      HttpServletResponse response) {
    return analysisFacade.overviewExport(id, response);
  }

}
