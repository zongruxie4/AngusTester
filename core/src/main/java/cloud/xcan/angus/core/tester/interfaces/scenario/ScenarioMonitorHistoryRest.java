package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioMonitorHistoryFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ScenarioMonitorHistory")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/monitor/history")
public class ScenarioMonitorHistoryRest {

  @Resource
  private ScenarioMonitorHistoryFacade scenarioMonitorHistoryFacade;

  @Operation(description = "Query the detail of scenario monitor history", operationId = " scenario:monitor:history:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorHistoryDetailVo> detail(
      @Parameter(name = "id", description = "Scenario monitor history id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.detail(id));
  }

  @Operation(description = "Query the list of scenario monitor history", operationId = " scenario:monitor:history:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorHistoryListVo>> list(
      @Valid ScenarioMonitorHistoryFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.list(dto));
  }

}
