package cloud.xcan.angus.core.tester.domain.project.evaluation;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EvaluationSearchRepo extends CustomBaseRepository<TestEvaluation> {

}

