package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.trash.ApisTrashSearchDto;
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

@Tag(name = "ApisTrash", description = "API Recycle Bin Management - Temporary storage for deleted service and apis with restore capabilities and permanent deletion controls.")
@Validated
@RestController
@RequestMapping("/api/v1/apis/trash")
public class ApisTrashRest {

  @Resource
  private ApisTrashFacade apisTrashFacade;

  @Operation(summary = "Clear the trash of services or apis", operationId = "apis:trash:clear")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.clear(id);
  }

  @Operation(summary = "Clear all the trash of services and api apis", operationId = "apis:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping()
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    apisTrashFacade.clearAll(projectId);
  }

  @Operation(summary = "Back the services or apis from the trash", operationId = "apis:trash:back")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    apisTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Back all the services and apis from the trash", operationId = "apis:trash:back:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    apisTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the number of all services and api trash", operationId = "apis:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisTrashFacade.count(projectId));
  }

  @Operation(summary = "Fulltext search the trash of services or apis", operationId = "apis:trash:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisTrashDetailVo>> search(
      @Valid @ParameterObject ApisTrashSearchDto dto) {
    return ApiLocaleResult.success(apisTrashFacade.search(dto));
  }

}
