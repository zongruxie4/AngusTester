package cloud.xcan.angus.core.tester.interfaces.project;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.project.facade.TagFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.TagVo;
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

@Tag(name = "Tag", description = "Project Tag Management API - Classification system for organizing and categorizing project resources with flexible tagging capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/tag")
public class TagRest {

  @Resource
  private TagFacade tagFacade;

  @Operation(summary = "Create new tags", operationId = "tag:add", description = "Create multiple new tags for project resource classification and organization")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Tags created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody TagAddDto dto) {
    return ApiLocaleResult.success(tagFacade.add(dto));
  }

  @Operation(summary = "Update existing tags", operationId = "tag:update", description = "Update multiple tag names and properties for improved classification")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tags updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<TagUpdateDto> dto) {
    tagFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import tag examples", operationId = "tag:example:import", description = "Import predefined tag examples to jumpstart project classification system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Tag examples imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for tag example import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(tagFacade.importExample(projectId));
  }

  @Operation(summary = "Delete tags", operationId = "tag:delete", description = "Permanently remove multiple tags from the project classification system")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Tags deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Tag identifiers for deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    tagFacade.delete(ids);
  }

  @Operation(summary = "Get tag details", operationId = "tag:detail", description = "Retrieve details of a specific tag including usage statistics")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tag details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Tag not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<TagVo> detail(
      @Parameter(name = "id", description = "Tag identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(tagFacade.detail(id));
  }

  @Operation(summary = "List tags", operationId = "tag:list", description = "Retrieve paginated list of project tags with filtering and search capabilities")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tag list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<TagVo>> list(@Valid TagFindDto dto) {
    return ApiLocaleResult.success(tagFacade.list(dto));
  }

}
