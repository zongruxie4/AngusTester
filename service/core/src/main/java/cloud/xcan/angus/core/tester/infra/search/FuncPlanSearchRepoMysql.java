package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncPlanSearchRepoMysql extends SimpleSearchRepository<FuncPlan> implements
    FuncPlanSearchRepo {

}

