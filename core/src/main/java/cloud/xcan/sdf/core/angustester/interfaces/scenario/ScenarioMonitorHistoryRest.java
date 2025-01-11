package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioMonitorHistoryFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

  @ApiOperation(value = "Query the detail of scenario monitor history", nickname = " scenario:monitor:history:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorHistoryDetailVo> detail(
      @ApiParam(name = "id", value = "Scenario monitor history id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of scenario monitor history", nickname = " scenario:monitor:history:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorHistoryListVo>> list(
      @Valid ScenarioMonitorHistoryFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorHistoryFacade.list(dto));
  }

}
