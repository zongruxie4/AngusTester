package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.project;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import org.springframework.stereotype.Repository;

@Repository("evaluationRepo")
public interface EvaluationRepoMysql extends EvaluationRepo {

}

