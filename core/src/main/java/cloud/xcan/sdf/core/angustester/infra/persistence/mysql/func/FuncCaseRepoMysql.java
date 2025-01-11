package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.func;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseRepo")
public interface FuncCaseRepoMysql extends FuncCaseRepo {


}
