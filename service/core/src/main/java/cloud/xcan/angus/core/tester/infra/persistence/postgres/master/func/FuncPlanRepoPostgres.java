package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.func;

import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import org.springframework.stereotype.Repository;

@Repository("funcPlanRepo")
public interface FuncPlanRepoPostgres extends FuncPlanRepo {


}
