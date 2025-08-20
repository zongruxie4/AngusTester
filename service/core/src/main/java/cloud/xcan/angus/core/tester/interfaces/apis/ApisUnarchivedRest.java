package cloud.xcan.angus.core.tester.interfaces.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisUnarchivedFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedListVo;
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
import java.util.List;
import org.hibernate.validator.constraints.Length;
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

@Tag(name = "APIs Unarchived", description = "Personal Workspace Management - APIs for private staging area management of uncommitted APIs and personal development artifacts with lifecycle control")
@Validated
@RestController
@RequestMapping("/api/v1/apis/unarchived")
public class ApisUnarchivedRest {

  @Resource
  private ApisUnarchivedFacade apisUnarchivedFacade;

  @Operation(summary = "Create unarchived API", 
      description = "Create new unarchived API in personal workspace with comprehensive configuration and validation",
      operationId = "apis:unarchived:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Unarchived API created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisUnarchivedAddDto> dto) {
    return ApiLocaleResult.success(apisUnarchivedFacade.add(dto));
  }

  @Operation(summary = "Update unarchived API", 
      description = "Update existing unarchived API with comprehensive configuration and validation",
      operationId = "apis:unarchived:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unarchived API updated successfully"),
      @ApiResponse(responseCode = "404", description = "Unarchived API not found")
  })
  @PatchMapping("/{id}")
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ApisUnarchivedUpdateDto> dto) {
    apisUnarchivedFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Rename unarchived API", 
      description = "Update unarchived API name with comprehensive validation and metadata management",
      operationId = "apis:unarchived:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unarchived API name updated successfully"),
      @ApiResponse(responseCode = "404", description = "Unarchived API not found")})
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Unarchived API identifier for name update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New unarchived API name for identification", required = true) @Valid @Length(max = MAX_NAME_LENGTH_X2) @RequestParam("name") String name) {
    apisUnarchivedFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete unarchived API", 
      description = "Remove specific unarchived API from personal workspace with comprehensive cleanup",
      operationId = "apis:unarchived:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Unarchived API deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Unarchived API identifier for deletion", required = true) @PathVariable("id") Long id) {
    apisUnarchivedFacade.delete(id);
  }

  @Operation(summary = "Delete all unarchived APIs", 
      description = "Remove all unarchived APIs from personal workspace with comprehensive cleanup",
      operationId = "apis:unarchived:delete:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All unarchived APIs deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void deleteAll() {
    apisUnarchivedFacade.deleteAll();
  }

  @Operation(summary = "Get unarchived API details", 
      description = "Retrieve comprehensive unarchived API details including configuration and metadata",
      operationId = "apis:unarchived:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unarchived API details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Unarchived API not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisUnarchivedDetailVo> detail(
      @Parameter(name = "id", description = "Unarchived API identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisUnarchivedFacade.detail(id));
  }

  @Operation(summary = "Get unarchived API count", 
      description = "Retrieve total count of unarchived APIs for specific project with comprehensive statistics",
      operationId = "apis:unarchived:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unarchived API count retrieved successfully")})
  @GetMapping(value = "/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for unarchived API count") Long projectId) {
    return ApiLocaleResult.success(apisUnarchivedFacade.count(projectId));
  }

  @Operation(summary = "Query unarchived API list", 
      description = "Retrieve paginated list of unarchived APIs with comprehensive filtering and search options",
      operationId = "apis:unarchived:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unarchived API list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisUnarchivedListVo>> list(
      @Valid @ParameterObject ApisUnarchivedFindDto dto) {
    return ApiLocaleResult.success(apisUnarchivedFacade.list(dto));
  }

}
