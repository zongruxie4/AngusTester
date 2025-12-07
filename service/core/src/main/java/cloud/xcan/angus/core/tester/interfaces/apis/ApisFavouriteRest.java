package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.favourite.ApisFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
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

@Tag(name = "API Favorites", description = "API Favorites Management - APIs for personal bookmark management, quick access classification, and frequently used API endpoint organization")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisFavouriteRest {

  @Resource
  private ApisFavouriteFacade apisFavouriteFacade;

  @Operation(summary = "Add API to favorites",
      description = "Add specific API to user favorites for quick access and personal organization",
      operationId = "apis:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API added to favorites successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @Parameter(name = "apiId", description = "API identifier for favorite addition", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFavouriteFacade.add(apiId));
  }

  @Operation(summary = "Remove API from favorites",
      description = "Remove specific API from user favorites with proper cleanup and validation",
      operationId = "apis:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API removed from favorites successfully"),
      @ApiResponse(responseCode = "404", description = "API favorite not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/favourite")
  public void cancel(
      @Parameter(name = "apiId", description = "API identifier for favorite removal", required = true) @PathVariable("apiId") Long apiId) {
    apisFavouriteFacade.cancel(apiId);
  }

  @Operation(summary = "Remove all API favorites",
      description = "Remove all API favorites for specific project with comprehensive cleanup",
      operationId = "apis:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All API favorites removed successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for favorite cleanup") Long projectId) {
    apisFavouriteFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query API favorites list",
      description = "Retrieve paginated list of user API favorites with comprehensive filtering and search options",
      operationId = "apis:favourite:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API favorites list retrieved successfully")})
  @GetMapping("/favourite")
  public ApiLocaleResult<PageResult<ApisFavouriteDetailVo>> list(
      @Valid @ParameterObject ApisFavouriteFindDto dto) {
    return ApiLocaleResult.success(apisFavouriteFacade.list(dto));
  }

  @Operation(summary = "Get API favorites count",
      description = "Retrieve total count of API favorites for specific project with comprehensive statistics",
      operationId = "apis:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API favorites count retrieved successfully")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for favorite count") Long projectId) {
    return ApiLocaleResult.success(apisFavouriteFacade.count(projectId));
  }
}
