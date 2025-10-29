package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.test;

import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("funcCaseInfoRepo")
public interface FuncCaseInfoRepoMysql extends FuncCaseInfoRepo {


}
