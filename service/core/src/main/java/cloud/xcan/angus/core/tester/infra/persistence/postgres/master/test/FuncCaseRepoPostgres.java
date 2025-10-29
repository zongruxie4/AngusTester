package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.test;

import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseRepo")
public interface FuncCaseRepoPostgres extends FuncCaseRepo {

}
