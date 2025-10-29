package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.test;

import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanRepo;
import org.springframework.stereotype.Repository;

@Repository("funcPlanRepo")
public interface FuncPlanRepoMysql extends FuncPlanRepo {


}
