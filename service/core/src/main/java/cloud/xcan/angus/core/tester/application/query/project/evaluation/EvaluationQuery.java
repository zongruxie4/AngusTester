package cloud.xcan.angus.core.tester.application.query.project.evaluation;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface EvaluationQuery {

  TestEvaluation detail(Long id);

  TestEvaluation checkAndFind(Long id);

  Page<TestEvaluation> list(GenericSpecification<TestEvaluation> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);
}

