package cloud.xcan.angus.core.tester.interfaces.tag;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.tag.facade.TagFacade;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagAddDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.vo.TagVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
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

@Tag(name = "Tag", description = "Project Tag Management - Manage organizational tags and classification systems for project resources.")
@Validated
@RestController
@RequestMapping("/api/v1/tag")
public class TagRest {

  @Resource
  private TagFacade tagFacade;

  @Operation(description = "Add the tag", operationId = "tag:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody TagAddDto dto) {
    return ApiLocaleResult.success(tagFacade.add(dto));
  }

  @Operation(description = "Update the tag", operationId = "tag:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<TagUpdateDto> dto) {
    tagFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Import the inner tag example", operationId = "tag:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(tagFacade.importExample(projectId));
  }

  @Operation(description = "Delete the tags", operationId = "tag:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Case tag ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    tagFacade.delete(ids);
  }

  @Operation(description = "Query the detail of tag", operationId = "tag:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TagVo> detail(
      @Parameter(name = "id", description = "Tag id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(tagFacade.detail(id));
  }

  @Operation(description = "Query the tag list", operationId = "tag:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TagVo>> list(@Valid TagFindDto dto) {
    return ApiLocaleResult.success(tagFacade.list(dto));
  }

  @Operation(description = "Fulltext search the tag", operationId = "tag:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<TagVo>> search(@Valid TagSearchDto dto) {
    return ApiLocaleResult.success(tagFacade.search(dto));
  }
}
