package cloud.xcan.angus.core.tester.interfaces.kanban;

import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanTestEvaluationFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanTestEvaluationDto;
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

@Tag(name = "Kanban - TestEvaluation", description = "Test Evaluation Analytics - Verify whether the object meets preset standards, identify problems and support decision-making optimization")
@Validated
@RestController
@RequestMapping("/api/v1/kanban/test-evaluation")
public class KanbanTestEvaluationRest {

  @Resource
  private KanbanTestEvaluationFacade kanbanTestEvaluationFacade;

  @Operation(summary = "Generate testing evaluation statistics overview",
      description = "Provides testing evaluation analysis to verify whether the object meets preset standards, identify problems and support decision-making optimization",
      operationId = "kanban:test-evaluation:overview")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Testing evaluation statistics overview generated successfully")})
  @GetMapping("/overview")
  public ApiLocaleResult<TestEvaluationResult> testEvaluationOverview(
      @Valid @ParameterObject KanbanTestEvaluationDto dto) {
    return ApiLocaleResult.success(kanbanTestEvaluationFacade.testEvaluationOverview(dto));
  }
}
