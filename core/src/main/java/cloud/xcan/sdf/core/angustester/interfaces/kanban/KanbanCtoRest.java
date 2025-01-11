package cloud.xcan.sdf.core.angustester.interfaces.kanban;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoTaskOverview;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.KanbanCtoFacade;
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

@Api(tags = "KanbanCto")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/cto")
public class KanbanCtoRest {

  @Resource
  private KanbanCtoFacade kanbanCtoFacade;

  @ApiOperation(value = "Task efficiency statistics overview for cto", nickname = "kanban:cto:task:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/task/overview")
  public ApiLocaleResult<CtoTaskOverview> taskCtoOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.taskCtoOverview(dto));
  }

  @ApiOperation(value = "Case statistics overview for cto", nickname = "kanban:cto:case:overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/case/overview")
  public ApiLocaleResult<CtoCaseOverview> caseEfficiencyOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.caseEfficiencyOverview(dto));
  }

}
