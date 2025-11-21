package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationSearchRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import org.springframework.stereotype.Repository;

@Repository
public class EvaluationSearchRepoMysql extends SimpleSearchRepository<TestEvaluation>
    implements EvaluationSearchRepo {

}

