package cloud.xcan.sdf.core.angustester.interfaces.tag;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.TagFacade;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.vo.TagVo;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Tag")
@Validated
@RestController
@RequestMapping("/api/v1/tag")
public class TagRest {

  @Resource
  private TagFacade tagFacade;

  @ApiOperation(value = "Add the tag", nickname = "tag:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody TagAddDto dto) {
    return ApiLocaleResult.success(tagFacade.add(dto));
  }

  @ApiOperation(value = "Update the tag", nickname = "tag:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<TagUpdateDto> dto) {
    tagFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the tags", nickname = "tag:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Case tag ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    tagFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of tag", nickname = "tag:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TagVo> detail(
      @ApiParam(name = "id", value = "Tag id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(tagFacade.detail(id));
  }

  @ApiOperation(value = "Query the tag list", nickname = "tag:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<TagVo>> list(@Valid TagFindDto dto) {
    return ApiLocaleResult.success(tagFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the tag", nickname = "tag:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TagVo>> search(@Valid TagSearchDto dto) {
    return ApiLocaleResult.success(tagFacade.search(dto));
  }
}
