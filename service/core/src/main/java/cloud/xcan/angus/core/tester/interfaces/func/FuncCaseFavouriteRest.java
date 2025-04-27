package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FuncCaseFavourite", description = "Test Case Favorites Management - Quick-access mechanism for bookmarking frequently used test cases.")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFavouriteRest {

  @Resource
  private FuncCaseFavouriteFacade funcCaseFavouriteFacade;

  @Operation(description = "Add the favourite of case", operationId = "func:case:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Favourite successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @Parameter(name = "caseId", description = "Case id", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.add(caseId));
  }

  @Operation(description = "Cancel the favourite of case", operationId = "func:case:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Canceled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/favourite")
  public void cancel(
      @Parameter(name = "caseId", description = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFavouriteFacade.cancel(caseId);
  }

  @Operation(description = "Cancel all the favourite of case", operationId = "func:case:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    funcCaseFavouriteFacade.cancelAll(projectId);
  }

  @Operation(description = "Fulltext search case favourite", operationId = "func:case:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<FuncCaseFavouriteDetailVo>> search(
      FuncCaseFavouriteSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.search(dto));
  }

  @Operation(description = "Query the favourite number of case", operationId = "func:case:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.count(projectId));
  }
}
