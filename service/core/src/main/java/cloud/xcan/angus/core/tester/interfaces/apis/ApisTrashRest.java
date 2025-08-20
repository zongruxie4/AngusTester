package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.trash.ApisTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "API Recycle Bin", description = "API Recycle Bin Management - APIs for temporary storage of deleted service and API resources with restore capabilities, permanent deletion controls, and lifecycle management")
@Validated
@RestController
@RequestMapping("/api/v1/apis/trash")
public class ApisTrashRest {

  @Resource
  private ApisTrashFacade apisTrashFacade;

  @Operation(summary = "Clear API trash", 
      description = "Permanently remove specific API or service from trash with comprehensive cleanup",
      operationId = "apis:trash:clear")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "API trash cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash identifier for permanent deletion", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.clear(id);
  }

  @Operation(summary = "Clear all API trash", 
      description = "Permanently remove all API and service trash for specific project with comprehensive cleanup",
      operationId = "apis:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "All API trash cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping()
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for trash cleanup") Long projectId) {
    apisTrashFacade.clearAll(projectId);
  }

  @Operation(summary = "Restore API from trash", 
      description = "Restore specific API or service from trash with comprehensive validation and lifecycle management",
      operationId = "apis:trash:back")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "API restored from trash successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash identifier for restoration", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restore all APIs from trash", 
      description = "Restore all APIs and services from trash for specific project with comprehensive validation",
      operationId = "apis:trash:back:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All APIs restored from trash successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for trash restoration") Long projectId) {
    apisTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get API trash count", 
      description = "Retrieve total count of API and service trash for specific project with comprehensive statistics",
      operationId = "apis:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API trash count retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for trash count") Long projectId) {
    return ApiLocaleResult.success(apisTrashFacade.count(projectId));
  }

  @Operation(summary = "Search API trash", 
      description = "Full-text search API and service trash with comprehensive filtering and search options",
      operationId = "apis:trash:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API trash search results retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisTrashDetailVo>> list(
      @Valid @ParameterObject ApisTrashFindDto dto) {
    return ApiLocaleResult.success(apisTrashFacade.list(dto));
  }

}
