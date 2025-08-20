package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
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

@Tag(name = "Scenario Favorites", description = "Scenario Favorites Management API - Quick-access bookmarking system for frequently used test scenarios with organizational capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioFavoriteRest {

  @Resource
  private ScenarioFavouriteFacade scenarioFavouriteFacade;

  @Operation(summary = "Add scenario to favorites",
      description = "Bookmark a scenario for quick access and easy retrieval",
      operationId = "scenario:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario added to favorites successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Scenario identifier for bookmarking", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.add(id));
  }

  @Operation(summary = "Remove scenario from favorites",
      description = "Remove scenario bookmark and clear from favorites list",
      operationId = "scenario:favourite:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario removed from favorites successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/favourite")
  public void cancel(
      @Parameter(name = "id", description = "Scenario identifier for removal from favorites", required = true) @PathVariable("id") Long id) {
    scenarioFavouriteFacade.cancel(id);
  }

  @Operation(summary = "Clear all scenario favorites",
      description = "Remove all scenario bookmarks for the specified project",
      operationId = "scenario:favourite:delete:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All scenario favorites cleared successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for bulk favorites removal") Long projectId) {
    scenarioFavouriteFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query scenario favorites list",
      description = "Retrieve paginated list of favorited scenarios with filtering capabilities",
      operationId = "scenario:favourite:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario favorites list retrieved successfully")})
  @GetMapping("/favourite")
  public ApiLocaleResult<PageResult<ScenarioFavouriteDetailVo>> list(
      @Valid @ParameterObject ScenarioFavouriteFindDto dto) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.list(dto));
  }

  @Operation(summary = "Query scenario favorites count",
      description = "Get total count of favorited scenarios for the specified project",
      operationId = "scenario:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario favorites count retrieved successfully")})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for favorites count") Long projectId) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.count(projectId));
  }
}
