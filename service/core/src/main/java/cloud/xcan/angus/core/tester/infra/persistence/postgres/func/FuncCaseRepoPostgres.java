package cloud.xcan.angus.core.tester.infra.persistence.postgres.func;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseRepo")
public interface FuncCaseRepoPostgres extends FuncCaseRepo {

}
