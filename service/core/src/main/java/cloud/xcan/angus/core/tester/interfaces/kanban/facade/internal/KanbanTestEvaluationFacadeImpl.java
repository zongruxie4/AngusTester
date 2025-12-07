package cloud.xcan.angus.core.tester.interfaces.kanban.facade.internal;

import static java.util.Objects.isNull;

import cloud.xcan.angus.core.tester.application.query.project.TestEvaluationQuery;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.KanbanTestEvaluationFacade;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanTestEvaluationDto;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KanbanTestEvaluationFacadeImpl implements KanbanTestEvaluationFacade {

  @Resource
  private TestEvaluationQuery testEvaluationQuery;

  @Override
  public TestEvaluationResult testEvaluationOverview(KanbanTestEvaluationDto dto) {
    TestEvaluation evaluation = new TestEvaluation();
    evaluation.setProjectId(dto.getProjectId());
    evaluation.setScope(
        isNull(dto.getPlanId()) ? EvaluationScope.PROJECT : EvaluationScope.FUNC_PLAN);
    evaluation.setResourceId(isNull(dto.getPlanId()) ? dto.getProjectId() : dto.getPlanId());
    evaluation.setPurposes(List.of(EvaluationPurpose.values()));
    evaluation.setStartDate(dto.getCreatedDateStart());
    evaluation.setDeadlineDate(dto.getCreatedDateEnd());
    return testEvaluationQuery.getEvaluationResult(evaluation, false);
  }
}
