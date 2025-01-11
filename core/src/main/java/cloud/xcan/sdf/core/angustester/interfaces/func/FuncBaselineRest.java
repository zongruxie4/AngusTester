package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncBaselineFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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

@Api(tags = "FuncBaseline")
@Validated
@RestController
@RequestMapping("/api/v1/func/baseline")
public class FuncBaselineRest {

  @Resource
  private FuncBaselineFacade funcBaselineFacade;

  @ApiOperation(value = "Add the baseline", nickname = "func:baseline:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncBaselineAddDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.add(dto));
  }

  @ApiOperation(value = "Update the baseline", nickname = "func:baseline:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncBaselineUpdateDto dto) {
    funcBaselineFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the baseline", nickname = "func:baseline:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncBaselineReplaceDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.replace(dto));
  }

  @ApiOperation(value = "Establish the baseline", nickname = "func:baseline:establish")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/establish")
  public ApiLocaleResult<?> establish(
      @ApiParam(name = "id", value = "Baseline id", required = true) @PathVariable("id") Long id) {
    funcBaselineFacade.establish(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the baselines", nickname = "func:baseline:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Case baseline ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcBaselineFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of baseline", nickname = "func:baseline:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncBaselineDetailVo> detail(
      @ApiParam(name = "id", value = "Baseline id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcBaselineFacade.detail(id));
  }

  @ApiOperation(value = "Query the baseline list", nickname = "func:baseline:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncBaselineVo>> list(@Valid FuncBaselineFindDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the baseline", nickname = "func:baseline:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncBaselineVo>> search(@Valid FuncBaselineSearchDto dto) {
    return ApiLocaleResult.success(funcBaselineFacade.search(dto));
  }

}
