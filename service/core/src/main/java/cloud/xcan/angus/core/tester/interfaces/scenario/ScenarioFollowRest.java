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

@Tag(name = "Scenario Follow", description = "Scenario Follow Management API - Notification and subscription system for tracking changes and updates to test scenarios")
@Validated
@RestController
@RequestMapping("/api/v1/scenario")
public class ScenarioFollowRest {

  @Resource
  private ScenarioFollowFacade scenarioFollowFacade;

  @Operation(summary = "Follow scenario for notifications",
      description = "Subscribe to scenario updates and receive notifications for changes",
      operationId = "scenario:follow:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario follow subscription created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/follow")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Scenario identifier for follow subscription", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioFollowFacade.add(id));
  }

  @Operation(summary = "Unfollow scenario",
      description = "Cancel follow subscription and stop receiving notifications for the scenario",
      operationId = "scenario:follow:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario follow subscription canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/follow")
  public void cancel(
      @Parameter(name = "id", description = "Scenario identifier for unfollowing", required = true) @PathVariable("id") Long id) {
    scenarioFollowFacade.cancel(id);
  }

  @Operation(summary = "Unfollow all scenarios",
      description = "Cancel all follow subscriptions for scenarios in the specified project",
      operationId = "scenario:follow:cancel:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All scenario follow subscriptions canceled successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/follow")
  public void cancelAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for bulk unfollowing") Long projectId) {
    scenarioFollowFacade.cancelAll(projectId);
  }

  @Operation(summary = "Query scenario follow list",
      description = "Retrieve paginated list of followed scenarios with filtering capabilities",
      operationId = "scenario:follow:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario follow list retrieved successfully")})
  @GetMapping("/follow")
  public ApiLocaleResult<PageResult<ScenarioFollowDetailVo>> list(
      @Valid @ParameterObject ScenarioFollowFindDto dto) {
    return ApiLocaleResult.success(scenarioFollowFacade.list(dto));
  }

  @Operation(summary = "Query scenario follow count",
      description = "Get total count of followed scenarios for the specified project",
      operationId = "scenario:follow:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario follow count retrieved successfully")})
  @GetMapping("/follow/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for follow count") Long projectId) {
    return ApiLocaleResult.success(scenarioFollowFacade.count(projectId));
  }
}
