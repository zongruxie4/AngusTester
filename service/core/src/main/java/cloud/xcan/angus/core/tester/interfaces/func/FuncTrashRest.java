package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.trash.FuncTrashSearchDto;
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

@Tag(name = "FuncTrash", description = "Functional Test Recycle Bin Management - Temporary storage for deleted plan and cases with restore capabilities and permanent deletion controls.")
@Validated
@RestController
@RequestMapping("/api/v1/func/trash")
public class FuncTrashRest {

  @Resource
  private FuncTrashFacade funcTrashFacade;

  @Operation(summary = "Clear the trash of functional testing", operationId = "func:trash:clear")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.clear(id);
  }

  @Operation(summary = "Clear all the trash of functional testing", operationId = "func:trash:clear:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    funcTrashFacade.clearAll(projectId);
  }

  @Operation(summary = "Back the functional testing from the trash", operationId = "func:trash:back")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash id", required = true) @PathVariable("id") Long id) {
    funcTrashFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Back all the functional testing from the trash", operationId = "func:trash:back:all")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Backed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    funcTrashFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the number of functional testing", operationId = "func:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcTrashFacade.count(projectId));
  }

  @Operation(summary = "Fulltext search the trash of functional testing", operationId = "func:trash:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncTrashDetailVo>> search(
      @Valid @ParameterObject FuncTrashSearchDto dto) {
    return ApiLocaleResult.success(funcTrashFacade.search(dto));
  }

}
