package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.project;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationSearchRepo;
import org.springframework.stereotype.Repository;

@Repository("evaluationSearchRepo")
public interface EvaluationSearchRepoPostgres extends EvaluationSearchRepo {

}

