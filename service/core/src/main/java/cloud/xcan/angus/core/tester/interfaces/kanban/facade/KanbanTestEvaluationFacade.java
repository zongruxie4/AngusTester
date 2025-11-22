package cloud.xcan.angus.core.tester.interfaces.kanban.facade;

import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanTestEvaluationDto;

public interface KanbanTestEvaluationFacade {

  TestEvaluationResult testEvaluationOverview(KanbanTestEvaluationDto dto);
}
