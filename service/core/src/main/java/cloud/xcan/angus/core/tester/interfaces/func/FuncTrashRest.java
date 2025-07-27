package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.trash.FuncTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.trash.FuncTrashDetailVo;
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

@Tag(name = "Functional Test Recycle Bin", description = "Functional Test Recycle Bin Management - Comprehensive APIs for temporary storage of deleted test plans and cases with restore capabilities and permanent deletion controls")
@Validated
@RestController
@RequestMapping("/api/v1/func/trash")
public class FuncTrashRest {

  @Resource
  private FuncTrashFacade funcTrashFacade;

  @Operation(summary = "Clear functional test trash item", 
      description = "Permanently remove a specific item from functional test recycle bin with no recovery option",
      operationId = "func:trash:clear")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Functional test trash item cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash item identifier for permanent deletion", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.clear(id);
  }

  @Operation(summary = "Clear all functional test trash", 
      description = "Permanently remove all items from functional test recycle bin for a specific project",
      operationId = "func:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "All functional test trash cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for trash cleanup") Long projectId) {
    funcTrashFacade.clearAll(projectId);
  }

  @Operation(summary = "Restore functional test item from trash", 
      description = "Recover a specific item from functional test recycle bin to its original location",
      operationId = "func:trash:back")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Functional test item restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash item identifier for restoration", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restore all functional test items from trash", 
      description = "Recover all items from functional test recycle bin for a specific project to their original locations",
      operationId = "func:trash:back:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All functional test items restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for bulk restoration") Long projectId) {
    funcTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get functional test trash count", 
      description = "Retrieve the total count of items in functional test recycle bin for a specific project",
      operationId = "func:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test trash count retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for count retrieval") Long projectId) {
    return ApiLocaleResult.success(funcTrashFacade.count(projectId));
  }

  @Operation(summary = "Search functional test trash", 
      description = "Perform full-text search on functional test recycle bin with comprehensive filtering and search options",
      operationId = "func:trash:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test trash search results retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncTrashDetailVo>> list(
      @Valid @ParameterObject FuncTrashFindDto dto) {
    return ApiLocaleResult.success(funcTrashFacade.list(dto));
  }

}
