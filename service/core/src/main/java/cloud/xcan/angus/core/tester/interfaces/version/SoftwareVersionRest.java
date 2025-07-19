package cloud.xcan.angus.core.tester.interfaces.version;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.core.tester.interfaces.version.facade.SoftwareVersionFacade;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionVo;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "SoftwareVersion", description =
    "Management Software Versions in Projects - Unified access entry for managing software versions within projects, "
        + "helping teams keep track of different releases and maintain version control effectively")
@Validated
@RestController
@RequestMapping("/api/v1/software/version")
public class SoftwareVersionRest {

  @Resource
  private SoftwareVersionFacade softwareVersionFacade;

  @Operation(summary = "Add software version", operationId = "software:version:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody SoftwareVersionAddDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.add(dto));
  }

  @Operation(summary = "Update software version", operationId = "software:version:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody SoftwareVersionUpdateDto dto) {
    softwareVersionFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace software version", operationId = "software:version:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody SoftwareVersionReplaceDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.replace(dto));
  }

  @Operation(summary = "Modify software version status", operationId = "software:version:status:modify")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PutMapping(value = "/{id}/status")
  public ApiLocaleResult<?> status(
      @Parameter(name = "id", description = "Version id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "Version status", required = true) @RequestParam("status")
      SoftwareVersionStatus status) {
    softwareVersionFacade.status(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Merge software version", operationId = "software:version:merge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PutMapping(value = "/merge")
  public ApiLocaleResult<?> merge(
      @Parameter(name = "formId", description = "Merge form version id", required = true) @RequestParam("formId") Long formId,
      @Parameter(name = "toId", description = "Merge to version id", required = true) @RequestParam("toId") Long toId) {
    softwareVersionFacade.merge(formId, toId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete software version", operationId = "software:version:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    softwareVersionFacade.delete(ids);
  }

  @Operation(summary = "Query the detail of software version", operationId = "software:version:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<SoftwareVersionDetailVo> detail(
      @Parameter(name = "id", description = "Version id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(softwareVersionFacade.detail(id));
  }

  @Operation(summary = "Query the list of software version", operationId = "software:version:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<SoftwareVersionVo>> list(
      @Valid @ParameterObject SoftwareVersionFindDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.list(dto));
  }

}
