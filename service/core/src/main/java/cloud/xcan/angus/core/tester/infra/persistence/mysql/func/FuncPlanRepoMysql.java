package cloud.xcan.angus.core.tester.infra.persistence.mysql.func;

import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import org.springframework.stereotype.Repository;

@Repository("funcPlanRepo")
public interface FuncPlanRepoMysql extends FuncPlanRepo {


}
