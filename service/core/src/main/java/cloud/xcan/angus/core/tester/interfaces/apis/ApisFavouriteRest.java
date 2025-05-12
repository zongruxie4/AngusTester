package cloud.xcan.angus.core.tester.interfaces.apis;


import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.favourite.ApisFavouriteSearchDto;
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

@Tag(name = "ApisFavourite", description = "API Favorites Management - Personal quick-access classification for bookmarking frequently used API endpoints.")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisFavouriteRest {

  @Resource
  private ApisFavouriteFacade apisFavouriteFacade;

  @Operation(description = "Add the favourite of apis", operationId = "apis:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Favourite successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{apiId}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @PathVariable("apiId") @Parameter(name = "apiId", description = "apis id", required = true) Long apiId) {
    return ApiLocaleResult.success(apisFavouriteFacade.add(apiId));
  }

  @Operation(description = "Cancel the favourite of apis", operationId = "apis:favourite:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Canceled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{apiId}/favourite")
  public void cancel(
      @Parameter(name = "apiId", description = "apis id", required = true) @PathVariable("apiId") Long apiId) {
    apisFavouriteFacade.cancel(apiId);
  }

  @Operation(description = "Cancel all the favourite of apis", operationId = "apis:favourite:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    apisFavouriteFacade.cancelAll(projectId);
  }

  @Operation(description = "Fulltext search apis favourite", operationId = "apis:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<ApisFavouriteDetailVo>> search(
      @Valid @ParameterObject ApisFavouriteSearchDto dto) {
    return ApiLocaleResult.success(apisFavouriteFacade.search(dto));
  }

  @Operation(description = "Query the favourite number of apis", operationId = "apis:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query number succeeded")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(apisFavouriteFacade.count(projectId));
  }
}
