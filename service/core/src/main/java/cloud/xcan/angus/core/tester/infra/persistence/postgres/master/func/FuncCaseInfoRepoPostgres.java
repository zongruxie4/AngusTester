package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.func;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoPostgres extends FuncCaseInfoRepo {

}
