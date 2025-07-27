package cloud.xcan.angus.core.tester.interfaces.kanban;


import cloud.xcan.angus.core.tester.domain.kanban.CtoCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanCtoFacade;
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

@Tag(name = "Kanban - CTO", description = "CTO-Level Dashboard Analytics - Executive dashboard endpoints for CTO-level strategic insights and performance metrics with comprehensive task completion trends and functional test KPIs for executive decision making")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/cto")
public class KanbanCtoRest {

  @Resource
  private KanbanCtoFacade kanbanCtoFacade;

  @Operation(summary = "Generate CTO-level task overview with executive metrics",
      description = "Provides comprehensive task completion analysis, resource utilization trends, and performance indicators for executive decision making and strategic planning",
      operationId = "kanban:cto:task:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "CTO task overview generated successfully with executive-level metrics")})
  @GetMapping("/task/overview")
  public ApiLocaleResult<CtoTaskOverview> taskCtoOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.taskCtoOverview(dto));
  }

  @Operation(summary = "Generate CTO-level case efficiency overview with executive metrics",
      description = "Provides comprehensive case efficiency analysis, quality metrics, and performance indicators for executive decision making and strategic planning",
      operationId = "kanban:cto:case:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "CTO case efficiency overview generated successfully with executive-level metrics")})
  @GetMapping("/case/overview")
  public ApiLocaleResult<CtoCaseOverview> caseEfficiencyOverview(
      @Valid @ParameterObject KanbanEfficiencyFindDto dto) {
    return ApiLocaleResult.success(kanbanCtoFacade.caseEfficiencyOverview(dto));
  }

}
