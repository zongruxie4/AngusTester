package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.func;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoMysql extends FuncCaseInfoRepo {


}
