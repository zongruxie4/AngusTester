package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.project;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import org.springframework.stereotype.Repository;

@Repository("evaluationRepo")
public interface EvaluationRepoPostgres extends EvaluationRepo {

}

