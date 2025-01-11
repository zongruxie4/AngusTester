package cloud.xcan.sdf.core.angustester.interfaces.apis;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases.ApisCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases.ApisCaseListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
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

@Api(tags = "ApisCase")
@Validated
@RestController
@RequestMapping("/api/v1/apis/case")
public class ApisCaseRest {

  @Resource
  private ApisCaseFacade apisCaseFacade;

  @ApiOperation(value = "Add apis functional test cases", nickname = "apis:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisCaseAddDto> dto) {
    return ApiLocaleResult.success(apisCaseFacade.add(dto));
  }

  @ApiOperation(value = "Update apis functional test cases", nickname = "apis:case:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisCaseUpdateDto> dto) {
    apisCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace apis functional test cases", nickname = "apis:case:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<?> replace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisCaseReplaceDto> dto) {
    apisCaseFacade.replace(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the name of apis functional test cases", nickname = "apis:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "name", value = "New case name", required = true) @Valid @Length(max = DEFAULT_NAME_LENGTH_X4) @RequestParam("name") String name) {
    apisCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the functional test cases", nickname = "func:case:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @ApiParam(name = "ids", value = "Case ids", required = true) @RequestParam(value = "enabled") Set<Long> ids,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisCaseFacade.enabled(ids, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Force synchronize the functional test cases to script", nickname = "func:case:script:sync")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Synchronized successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/syncToScript")
  public ApiLocaleResult<?> syncToScript(
      @ApiParam(name = "apisId", value = "Apis id", required = true) @RequestParam(value = "apisId") Long apisId) {
    apisCaseFacade.syncToScript(apisId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone the apis functional test cases", nickname = "apis:case:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Clone successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(apisCaseFacade.clone(ids));
  }

  @ApiOperation(value = "Delete apis functional test cases", nickname = "apis:case:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    apisCaseFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of apis functional test cases", nickname = "apis:case:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisCaseDetailVo> detail(
      @Valid @ApiParam(name = "id", value = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisCaseFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of apis functional test cases", nickname = "apis:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisCaseListVo>> list(@Valid ApisCaseFindDto dto) {
    return ApiLocaleResult.success(apisCaseFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of apis functional test cases", nickname = "apis:case:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisCaseListVo>> search(@Valid ApisCaseSearchDto dto) {
    return ApiLocaleResult.success(apisCaseFacade.search(dto));
  }

}