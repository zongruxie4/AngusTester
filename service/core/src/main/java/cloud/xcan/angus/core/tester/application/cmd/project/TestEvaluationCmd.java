package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TestEvaluationCmd {

  IdKey<Long, Object> add(TestEvaluation evaluation);

  void update(TestEvaluation evaluation);

  void generateResult(Long id);

  void delete(Long id);
}

