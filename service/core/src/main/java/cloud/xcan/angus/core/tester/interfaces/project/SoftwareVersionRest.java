package cloud.xcan.angus.core.tester.interfaces.project;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersionStatus;
import cloud.xcan.angus.core.tester.interfaces.project.facade.SoftwareVersionFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.SoftwareVersionVo;
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

@Tag(name = "Software Version", description = "Software Version Management API - Lifecycle management system for software version control, release planning, and deployment tracking across projects")
@Validated
@RestController
@RequestMapping("/api/v1/software/version")
public class SoftwareVersionRest {

  @Resource
  private SoftwareVersionFacade softwareVersionFacade;

  @Operation(summary = "Create new software version", operationId = "software:version:add", description = "Create a new software version with metadata including timeline planning and release documentation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Software version created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody SoftwareVersionAddDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.add(dto));
  }

  @Operation(summary = "Update software version", operationId = "software:version:update", description = "Update existing software version metadata including name, dates, and description")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software version updated successfully"),
      @ApiResponse(responseCode = "404", description = "Software version not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody SoftwareVersionUpdateDto dto) {
    softwareVersionFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace or create software version", operationId = "software:version:replace", description = "Replace existing software version or create new one based on identifier presence")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software version replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Software version not found")
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody SoftwareVersionReplaceDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.replace(dto));
  }

  @Operation(summary = "Modify software version status", operationId = "software:version:status:modify", description = "Update the lifecycle status of a software version for workflow management")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software version status modified successfully"),
      @ApiResponse(responseCode = "404", description = "Software version not found")})
  @PutMapping(value = "/{id}/status")
  public ApiLocaleResult<?> status(
      @Parameter(name = "id", description = "Software version identifier for status modification", required = true) @PathVariable("id") Long id,
      @Parameter(name = "status", description = "Target status for version lifecycle management", required = true) @RequestParam("status")
      SoftwareVersionStatus status) {
    softwareVersionFacade.status(id, status);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Merge software versions", operationId = "software:version:merge", description = "Merge source version into target version, consolidating version history and artifacts")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software versions merged successfully"),
      @ApiResponse(responseCode = "404", description = "Software version not found")})
  @PutMapping(value = "/merge")
  public ApiLocaleResult<?> merge(
      @Parameter(name = "formId", description = "Source version identifier for merge operation", required = true) @RequestParam("formId") Long formId,
      @Parameter(name = "toId", description = "Target version identifier for merge destination", required = true) @RequestParam("toId") Long toId) {
    softwareVersionFacade.merge(formId, toId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete software versions", operationId = "software:version:delete", description = "Permanently remove multiple software versions from the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Software versions deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    softwareVersionFacade.delete(ids);
  }

  @Operation(summary = "Get software version details", operationId = "software:version:detail", description = "Retrieve details of a specific software version including metadata and relationships")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software version details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Software version not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<SoftwareVersionDetailVo> detail(
      @Parameter(name = "id", description = "Software version identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(softwareVersionFacade.detail(id));
  }

  @Operation(summary = "List software versions", operationId = "software:version:list", description = "Retrieve paginated list of software versions with filtering and sorting capabilities")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Software version list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<SoftwareVersionVo>> list(
      @Valid @ParameterObject SoftwareVersionFindDto dto) {
    return ApiLocaleResult.success(softwareVersionFacade.list(dto));
  }

}
