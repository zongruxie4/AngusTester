package cloud.xcan.angus.core.tester.interfaces.func;


import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteFindDto;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Functional Test Case Favorites", description = "Functional Test Case Favorites Management - APIs for bookmarking and managing frequently used test cases with quick access and organization capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseFavouriteRest {

  @Resource
  private FuncCaseFavouriteFacade funcCaseFavouriteFacade;

  @Operation(summary = "Add test case to favorites", 
      description = "Add a specific test case to user favorites for quick access and organization",
      operationId = "func:case:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test case added to favorites successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{caseId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("caseId") @Parameter(name = "caseId", description = "Test case identifier for favorite addition", required = true) Long caseId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.add(caseId));
  }

  @Operation(summary = "Remove test case from favorites", 
      description = "Remove a specific test case from user favorites to clean up favorite list",
      operationId = "func:case:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case removed from favorites successfully"),
      @ApiResponse(responseCode = "404", description = "Test case favorite not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{caseId}/favourite")
  public void cancel(
      @Parameter(name = "caseId", description = "Test case identifier for favorite removal", required = true) @PathVariable("caseId") Long caseId) {
    funcCaseFavouriteFacade.cancel(caseId);
  }

  @Operation(summary = "Remove all test case favorites", 
      description = "Remove all test case favorites for a specific project to reset favorite list",
      operationId = "func:case:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All test case favorites removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for favorite cleanup") Long projectId) {
    funcCaseFavouriteFacade.cancelAll(projectId);
  }

  @Operation(summary = "List favorite test cases", 
      description = "Retrieve paginated list of favorite test cases with comprehensive filtering and search options",
      operationId = "func:case:favourite:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Favorite test case list retrieved successfully")})
  @GetMapping("/favourite")
  public ApiLocaleResult<PageResult<FuncCaseFavouriteDetailVo>> list(
      @ParameterObject FuncCaseFavouriteFindDto dto) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.list(dto));
  }

  @Operation(summary = "Get favorite test case count", 
      description = "Retrieve the total count of favorite test cases for a specific project",
      operationId = "func:case:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Favorite test case count retrieved successfully")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for count retrieval") Long projectId) {
    return ApiLocaleResult.success(funcCaseFavouriteFacade.count(projectId));
  }
}
