package cloud.xcan.sdf.core.angustester.interfaces.apis;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisUnarchivedFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisUnarchivedDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisUnarchivedListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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

@Api(tags = "ApisUnarchived")
@Validated
@RestController
@RequestMapping("/api/v1/apis/unarchived")
public class ApisUnarchivedRest {

  @Resource
  private ApisUnarchivedFacade apisUnarchivedFacade;

  @ApiOperation(value = "Add the unarchived apis", nickname = "apis:unarchived:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisUnarchivedAddDto> dtos) {
    return ApiLocaleResult.success(apisUnarchivedFacade.add(dtos));
  }

  @ApiOperation(value = "Update the unarchived apis", nickname = "apis:unarchived:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}")
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ApisUnarchivedUpdateDto> dtos) {
    apisUnarchivedFacade.update(dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the name of unarchived apis", nickname = "apis:unarchived:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Unarchived apis id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "name", value = "Unarchived apis name", required = true) @Valid @Length(max = DEFAULT_NAME_LENGTH_X2) @RequestParam("name") String name) {
    apisUnarchivedFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the unarchived apis", nickname = "apis:unarchived:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Unarchived apis id", required = true) @PathVariable("id") Long id) {
    apisUnarchivedFacade.delete(id);
  }

  @ApiOperation(value = "Delete all the unarchived apis", nickname = "apis:unarchived:delete:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void deleteAll() {
    apisUnarchivedFacade.deleteAll();
  }

  @ApiOperation(value = "Query the detail of unarchived apis", nickname = "apis:unarchived:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisUnarchivedDetailVo> detail(
      @ApiParam(name = "id", value = "Unarchived apis id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisUnarchivedFacade.detail(id));
  }

  @ApiOperation(value = "Query the number of unarchived apis", nickname = "apis:unarchived:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisUnarchivedFacade.count(projectId));
  }

  @ApiOperation(value = "Query the list of unarchived apis", nickname = "apis:unarchived:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisUnarchivedListVo>> list(
      @Valid ApisUnarchivedFindDto dto) {
    return ApiLocaleResult.success(apisUnarchivedFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the unarchived apis", nickname = "apis:unarchived:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisUnarchivedListVo>> search(
      @Valid ApisUnarchivedSearchDto dto) {
    return ApiLocaleResult.success(apisUnarchivedFacade.search(dto));
  }
}