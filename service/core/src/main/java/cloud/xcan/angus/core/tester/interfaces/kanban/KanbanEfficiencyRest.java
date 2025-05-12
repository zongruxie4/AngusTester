package cloud.xcan.angus.core.tester.interfaces.kanban;


import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanEfficiencyFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "KanbanEfficiency", description = "R&D & Test Efficiency Analytics - Statistical analysis of R&D task efficiency and functional test effectiveness.")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/efficiency")
public class KanbanEfficiencyRest {

  @Resource
  private KanbanEfficiencyFacade kanbanEfficiencyFacade;

  @Operation(description = "Task efficiency statistics overview", operationId = "kanban:efficiency:task:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/task/overview")
  public ApiLocaleResult<EfficiencyTaskOverview> taskEfficiencyOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.taskEfficiencyOverview(dto));
  }

  @Operation(description = "Case efficiency statistics overview", operationId = "kanban:efficiency:case:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/case/overview")
  public ApiLocaleResult<EfficiencyCaseOverview> caseEfficiencyOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.caseEfficiencyOverview(dto));
  }

}
