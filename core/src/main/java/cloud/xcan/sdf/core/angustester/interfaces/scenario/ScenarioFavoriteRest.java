package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.favorite.ScenarioFavouriteFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.favorite.ScenarioFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Api(tags = "ScenarioFavourite")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioFavoriteRest {

  @Resource
  private ScenarioFavouriteFacade scenarioFavouriteFacade;

  @ApiOperation(value = "Add the favourite of scenario", nickname = "scenario:favourite:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/favourite")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.add(id));
  }

  @ApiOperation(value = "Cancel the favourite of scenario", nickname = "scenario:favourite:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/favourite")
  public void cancel(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    scenarioFavouriteFacade.cancel(id);
  }

  @ApiOperation(value = "Cancel all the favorites of scenario", nickname = "scenario:favourite:delete:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/favourite")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    scenarioFavouriteFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Query the favourite list of scenario", nickname = "scenario:favourite:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/search")
  public ApiLocaleResult<PageResult<ScenarioFavouriteDetailVo>> search(
      @Valid ScenarioFavouriteFindDto dto) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.search(dto));
  }

  @ApiOperation(value = "Query the favourite number of scenario", nickname = "scenario:favourite:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/favourite/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(scenarioFavouriteFacade.count(projectId));
  }
}
