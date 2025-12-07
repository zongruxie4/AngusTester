package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TestEvaluationQuery {

  TestEvaluation detail(Long id);

  TestEvaluation checkAndFind(Long id);

  Page<TestEvaluation> list(GenericSpecification<TestEvaluation> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  TestEvaluationResult getEvaluationResult(TestEvaluation evaluation, boolean includeDetails);

  void setResourceName(List<TestEvaluation> evaluations);
}

