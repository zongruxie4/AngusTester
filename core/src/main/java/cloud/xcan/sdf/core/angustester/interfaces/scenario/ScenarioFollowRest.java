package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
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

@Api(tags = "ScenarioFollow")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioFollowRest {

  @Resource
  private ScenarioFollowFacade scenarioFollowFacade;

  @ApiOperation(value = "Add the follow of scenario", nickname = "scenario:follow:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Follow successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFollowFacade.add(id));
  }

  @ApiOperation(value = "Cancel the follow of scenario", nickname = "scenario:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @ApiParam(name = "id", value = "Scenario id", required = true) @PathVariable("id") Long id) {
    scenarioFollowFacade.cancel(id);
  }

  @ApiOperation(value = "Cancel all the follows of scenario", nickname = "scenario:follow:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    scenarioFollowFacade.cancelAll(projectId);
  }

  @ApiOperation(value = "Query the follow list of scenario", nickname = "scenario:follow:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<ScenarioFollowDetailVo>> search(
      @Valid ScenarioFollowFindDto dto) {
    return ApiLocaleResult.success(scenarioFollowFacade.search(dto));
  }

  @ApiOperation(value = "Query the follow number of scenario", nickname = "scenario:follow:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Query number succeeded", response = ApiLocaleResult.class)})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId) {
    return ApiLocaleResult.success(scenarioFollowFacade.count(projectId));
  }
}
