package cloud.xcan.angus.core.tester.infra.persistence.mysql.func;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoMysql extends FuncCaseInfoRepo {


}
