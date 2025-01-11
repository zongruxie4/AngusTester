package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.func;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoPostgres extends FuncCaseInfoRepo {

}
