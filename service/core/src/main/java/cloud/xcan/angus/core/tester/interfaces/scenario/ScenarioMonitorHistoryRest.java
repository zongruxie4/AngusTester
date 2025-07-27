package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorHistoryFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Scenario Monitor History", description = "Scenario Monitoring History API - Historical data and audit trail for scenario monitoring activities, including metrics, alerts, and resolution timelines.")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/monitor/history")
public class ScenarioMonitorHistoryRest {

  @Resource
  private ScenarioMonitorHistoryFacade scenarioMonitorHistoryFacade;

  @Operation(summary = "Query scenario monitor history detail",
      description = "Retrieve detailed historical record of a specific monitoring event or alert.",
      operationId = "scenario:monitor:history:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor history detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Monitor history record not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorHistoryDetailVo> detail(
      @Parameter(name = "id", description = "Monitor history record identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.detail(id));
  }

  @Operation(summary = "Query scenario monitor history list",
      description = "Retrieve paginated list of monitoring history records with filtering and search capabilities.",
      operationId = "scenario:monitor:history:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitor history list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorHistoryListVo>> list(
      @Valid @ParameterObject ScenarioMonitorHistoryFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.list(dto));
  }

}
