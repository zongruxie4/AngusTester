package cloud.xcan.sdf.core.angustester.interfaces.mock;

import static cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisAssembler.assembleAllowImportSampleStatus;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.MockApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.MockApisListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
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

@Api(tags = "MockApis")
@Validated
@RestController
@RequestMapping("/api/v1/mock/apis")
public class MockApisRest {

  @Resource
  private MockApisFacade mockApisFacade;

  @ApiOperation(value = "Add mock apis", nickname = "mock:apis:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<MockApisAddDto> dtos) {
    return ApiLocaleResult.success(mockApisFacade.add(dtos));
  }

  @ApiOperation(value = "Update mock apis", nickname = "mock:apis:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<MockApisUpdateDto> dto) {
    mockApisFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace mock apis", nickname = "mock:apis:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<MockApisReplaceDto> dto) {
    return ApiLocaleResult.success(mockApisFacade.replace(dto));
  }

  @ApiOperation(value = "Clone mock apis", nickname = "mock:apis:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Mock apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.clone(id));
  }

  @ApiOperation(value = "Move the apis to another mock service", nickname = "mock:apis:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Moved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @ApiParam(name = "targetServiceId", value = "Target mock service id", required = true) @RequestParam("targetServiceId") Long targetServiceId) {
    mockApisFacade.move(ids, targetServiceId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Sync the apis to mock service instance", nickname = "mock:apis:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Synchronized successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @ApiParam(name = "id", value = "Mock apis id", required = true) @PathVariable("id") Long id) {
    mockApisFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Generate mock apis from copied apis", nickname = "mock:apis:copy:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/copy/{apisId}/{mockServiceId}")
  public ApiLocaleResult<IdKey<Long, Object>> copyApisAdd(
      @ApiParam(name = "mockServiceId", value = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @ApiParam(name = "apisId", value = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.copyApisAdd(mockServiceId, apisId));
  }

  @ApiOperation(value = "Generate associated mock apis from apis", nickname = "mock:apis:association:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/{mockServiceId}/{apisId}")
  public ApiLocaleResult<IdKey<Long, Object>> assocApisAdd(
      @ApiParam(name = "mockServiceId", value = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @ApiParam(name = "apisId", value = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    return ApiLocaleResult.success(mockApisFacade.assocApisAdd(mockServiceId, apisId));
  }

  @ApiOperation(value = "Associate mock apis and apis", nickname = "mock:apis:association:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/association/{mockApisId}/{apisId}")
  public ApiLocaleResult<?> assocApisUpdate(
      @ApiParam(name = "mockApisId", value = "Mock apis id", required = true) @PathVariable("mockApisId") Long mockApisId,
      @ApiParam(name = "apisId", value = "Apis id", required = true) @PathVariable("apisId") Long apisId) {
    mockApisFacade.assocApisUpdate(mockApisId, apisId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the association between mock apis and apis", nickname = "mock:apis:association:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/association")
  public void assocDelete(@ApiParam(name = "ids", value = "Mock apis ids", required = true)
  @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.assocDelete(ids);
  }

  @ApiOperation(value = "Delete mock apis", nickname = "mock:apis:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Mock apis ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    mockApisFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of mock apis", nickname = "mock:apis:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockApisDetailVo> detail(
      @ApiParam(name = "id", value = "Mock apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockApisFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of mock apis", nickname = "mock:apis:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<MockApisListVo>> list(@Valid MockApisFindDto dto) {
    PageResult<MockApisListVo> result = mockApisFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @ApiOperation(value = "Fulltext search the list of mock apis", nickname = "mock:apis:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<MockApisListVo>> search(@Valid MockApisSearchDto dto) {
    PageResult<MockApisListVo> result = mockApisFacade.search(dto);
    return assembleAllowImportSampleStatus(result);
  }

}
