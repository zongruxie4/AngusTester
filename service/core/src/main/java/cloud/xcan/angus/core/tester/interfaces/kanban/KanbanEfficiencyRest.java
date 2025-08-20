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

@Tag(name = "Kanban - Efficiency", description = "Efficiency Analytics - Statistical analysis of R&D task efficiency and functional test effectiveness with performance metrics and productivity insights")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/efficiency")
public class KanbanEfficiencyRest {

  @Resource
  private KanbanEfficiencyFacade kanbanEfficiencyFacade;

  @Operation(summary = "Generate task efficiency statistics overview",
      description = "Provides task efficiency analysis including completion rates, time tracking, and productivity metrics for performance optimization",
      operationId = "kanban:efficiency:task:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task efficiency statistics overview generated successfully")})
  @GetMapping("/task/overview")
  public ApiLocaleResult<EfficiencyTaskOverview> taskEfficiencyOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.taskEfficiencyOverview(dto));
  }

  @Operation(summary = "Generate testing efficiency statistics overview",
      description = "Provides testing efficiency analysis including case execution rates, quality metrics, and effectiveness indicators for process improvement",
      operationId = "kanban:efficiency:case:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Testing efficiency statistics overview generated successfully")})
  @GetMapping("/case/overview")
  public ApiLocaleResult<EfficiencyCaseOverview> caseEfficiencyOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanEfficiencyFacade.caseEfficiencyOverview(dto));
  }

}
