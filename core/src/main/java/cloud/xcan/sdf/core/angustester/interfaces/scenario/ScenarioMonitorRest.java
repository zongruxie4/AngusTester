package cloud.xcan.sdf.core.angustester.interfaces.scenario;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioMonitorFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ScenarioMonitor")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/monitor")
public class ScenarioMonitorRest {

  @Resource
  private ScenarioMonitorFacade scenarioMonitorFacade;

  @ApiOperation(value = "Create scenario monitor", nickname = " scenario:monitor:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScenarioMonitorAddDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.add(dto));
  }

  @ApiOperation(value = "Update scenario monitor", nickname = " scenario:monitor:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ScenarioMonitorUpdateDto dto) {
    scenarioMonitorFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace scenario monitor", nickname = " scenario:monitor:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody ScenarioMonitorReplaceDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.replace(dto));
  }

  @ApiOperation(value = "Run scenario monitoring immediately", nickname = " scenario:monitor:run:now")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Generated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/{id}/run")
  public ApiLocaleResult<?> runNow(
      @ApiParam(name = "id", value = "Scenario monitor id", required = true) @PathVariable("id") Long id) {
    scenarioMonitorFacade.runNow(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete scenario monitor", nickname = " scenario:monitor:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Scenario monitor ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    scenarioMonitorFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of scenario monitor", nickname = " scenario:monitor:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScenarioMonitorDetailVo> detail(
      @ApiParam(name = "id", value = "Scenario monitor id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scenarioMonitorFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of scenario monitor", nickname = " scenario:monitor:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioMonitorListVo>> list(
      @Valid ScenarioMonitorFindDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of scenario monitor", nickname = " scenario:monitor:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ScenarioMonitorListVo>> search(
      @Valid ScenarioMonitorSearchDto dto) {
    return ApiLocaleResult.success(scenarioMonitorFacade.search(dto));
  }

}
