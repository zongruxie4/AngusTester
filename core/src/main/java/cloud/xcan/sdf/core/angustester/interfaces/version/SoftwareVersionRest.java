package cloud.xcan.sdf.core.angustester.interfaces.version;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionStatus;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.SoftwareVersionFacade;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionVo;
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

@Api(tags = "SoftwareVersion")
@Validated
@RestController
@RequestMapping("/api/v1/software/version")
public class SoftwareVersionRest {

  @Resource
  private SoftwareVersionFacade softwareVersionFacade;

  @ApiOperation(value = "Add task software version", nickname = "software:version:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody SoftwareVersionAddDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.add(dto));
  }

  @ApiOperation(value = "Update task software version", nickname = "software:version:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody SoftwareVersionUpdateDto dto) {
    softwareVersionFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace task software version", nickname = "software:version:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody SoftwareVersionReplaceDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.replace(dto));
  }

  @ApiOperation(value = "Modify task software version status", nickname = "software:version:status:modify")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PutMapping(value = "/{id}/status")
  public ApiLocaleResult<?> status(
      @ApiParam(name = "id", value = "Version id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "status", value = "Version status", required = true) @RequestParam("status")
      SoftwareVersionStatus status) {
    softwareVersionFacade.status(id, status);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Merge task software version", nickname = "software:version:merge")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PutMapping(value = "/merge")
  public ApiLocaleResult<?> merge(
      @ApiParam(name = "formId", value = "Merge form version id", required = true) @RequestParam("formId") Long formId,
      @ApiParam(name = "toId", value = "Merge to version id", required = true) @RequestParam("toId") Long toId) {
    softwareVersionFacade.merge(formId, toId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete task software version", nickname = "software:version:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    softwareVersionFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of task software version", nickname = "software:version:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<SoftwareVersionDetailVo> detail(
      @ApiParam(name = "id", value = "Version id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(softwareVersionFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of task software version", nickname = "software:version:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<SoftwareVersionVo>> list(@Valid SoftwareVersionFindDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of task software version", nickname = "software:version:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<SoftwareVersionVo>> search(@Valid SoftwareVersionSearchDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.search(dto));
  }

}
