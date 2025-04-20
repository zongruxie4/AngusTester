package cloud.xcan.angus.core.tester.interfaces.kanban;


import cloud.xcan.angus.core.tester.domain.kanban.CtoCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanCtoFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

  @Operation(description = "Task efficiency statistics overview for cto", operationId = "kanban:cto:task:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/task/overview")
  public ApiLocaleResult<CtoTaskOverview> taskCtoOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.taskCtoOverview(dto));
  }

  @Operation(description = "Case statistics overview for cto", operationId = "kanban:cto:case:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/case/overview")
  public ApiLocaleResult<CtoCaseOverview> caseEfficiencyOverview(
      @Valid KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.caseEfficiencyOverview(dto));
  }

}
