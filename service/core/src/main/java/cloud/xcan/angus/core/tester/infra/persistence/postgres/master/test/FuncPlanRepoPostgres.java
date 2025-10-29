package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.test;

import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanRepo;
import org.springframework.stereotype.Repository;

@Repository("funcPlanRepo")
public interface FuncPlanRepoPostgres extends FuncPlanRepo {


}
