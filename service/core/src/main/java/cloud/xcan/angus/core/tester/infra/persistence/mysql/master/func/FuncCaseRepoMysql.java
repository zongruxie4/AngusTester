package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.func;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseRepo")
public interface FuncCaseRepoMysql extends FuncCaseRepo {


}
