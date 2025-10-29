package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.test;

import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoPostgres extends FuncCaseInfoRepo {

}
