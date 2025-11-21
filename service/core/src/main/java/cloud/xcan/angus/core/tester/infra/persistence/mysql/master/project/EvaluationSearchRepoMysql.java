package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.project;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationSearchRepo;
import org.springframework.stereotype.Repository;

@Repository("evaluationSearchRepo")
public interface EvaluationSearchRepoMysql extends EvaluationSearchRepo {

}

