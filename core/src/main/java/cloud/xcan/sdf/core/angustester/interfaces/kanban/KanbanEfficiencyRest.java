package cloud.xcan.sdf.core.angustester.interfaces.kanban;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.KanbanEfficiencyFacade;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "KanbanEfficiency")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/efficiency")
public class KanbanEfficiencyRest {

  @Resource
  private KanbanEfficiencyFacade kanbanEfficiencyFacade;

  @ApiOperation(value = "Task efficiency statistics overview", nickname = "kanban:efficiency:task:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/task/overview")
  public ApiLocaleResult<EfficiencyTaskOverview> taskEfficiencyOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.taskEfficiencyOverview(dto));
  }

  @ApiOperation(value = "Case efficiency statistics overview", nickname = "kanban:efficiency:case:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/case/overview")
  public ApiLocaleResult<EfficiencyCaseOverview> caseEfficiencyOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.caseEfficiencyOverview(dto));
  }

}
