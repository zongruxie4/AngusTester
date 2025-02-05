package cloud.xcan.sdf.core.angustester.interfaces.data;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DatasourceFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceTestVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceVo;
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

@Api(tags = "DataDatasource")
@Validated
@RestController
@RequestMapping("/api/v1/data/datasource")
public class DatasourceRest {

  @Resource
  private DatasourceFacade datasourceFacade;

  @ApiOperation(value = "Add the datasource of data", nickname = "data:datasource:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody DatasourceAddDto dto) {
    return ApiLocaleResult.success(datasourceFacade.add(dto));
  }

  @ApiOperation(value = "Replace the datasource of data", nickname = "data:datasource:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody DatasourceReplaceDto dto) {
    return ApiLocaleResult.success(datasourceFacade.replace(dto));
  }

  @ApiOperation(value = "Delete the datasource of data", nickname = "data:datasource:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    datasourceFacade.delete(id);
  }

  @ApiOperation(value = "Test the configuration of data datasource", nickname = "data:datasource:sync:test:byId")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Tested successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/test")
  public ApiLocaleResult<DatasourceTestVo> testById(
      @ApiParam(name = "id", value = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.testById(id));
  }

  @ApiOperation(value = "Test the configuration of data datasource", nickname = "data:datasource:sync:test:byParam")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Tested successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/config/test")
  public ApiLocaleResult<DatasourceTestVo> testByConfig(@Valid DatasourceTestDto dto) {
    return ApiLocaleResult.success(datasourceFacade.testByConfig(dto));
  }

  @ApiOperation(value = "Query the detail of data datasource", nickname = "data:datasource:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<DatasourceDetailVo> detail(
      @ApiParam(name = "id", value = "Mock datasource id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(datasourceFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of data datasource", nickname = "data:datasource:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<DatasourceVo>> list(@Valid DatasourceFindDto dto) {
    return ApiLocaleResult.success(datasourceFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of data datasource", nickname = "data:datasource:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<DatasourceVo>> search(@Valid DatasourceSearchDto dto) {
    return ApiLocaleResult.success(datasourceFacade.search(dto));
  }
}