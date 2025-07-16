package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
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

@Tag(name = "ScenarioFollow", description = "Scenario Follow Management - Notification management interface for receiving alerts when subscribed test scenario are modified")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioFollowRest {

  @Resource
  private ScenarioFollowFacade scenarioFollowFacade;

  @Operation(summary = "Add the follow of scenario", operationId = "scenario:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Follow successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFollowFacade.add(id));
  }

  @Operation(summary = "Cancel the follow of scenario", operationId = "scenario:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @Parameter(name = "id", description = "Scenario id", required = true) @PathVariable("id") Long id) {
    scenarioFollowFacade.cancel(id);
  }

  @Operation(summary = "Cancel all the follows of scenario", operationId = "scenario:follow:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    scenarioFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query the follow list of scenario", operationId = "scenario:follow:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/follow/search")
  public ApiLocaleResult<PageResult<ScenarioFollowDetailVo>> search(
      @Valid @ParameterObject ScenarioFollowFindDto dto) {
    return ApiLocaleResult.success(scenarioFollowFacade.search(dto));
  }

  @Operation(summary = "Query the follow count of scenario", operationId = "scenario:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Query count succeeded")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project id") Long projectId) {
    return ApiLocaleResult.success(scenarioFollowFacade.count(projectId));
  }
}
