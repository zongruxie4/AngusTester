package cloud.xcan.angus.core.tester.domain.func.plan;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncPlanSearchRepo extends CustomBaseRepository<FuncPlan> {


}
