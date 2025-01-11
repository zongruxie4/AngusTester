package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.func;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseRepo")
public interface FuncCaseRepoPostgres extends FuncCaseRepo {

}
