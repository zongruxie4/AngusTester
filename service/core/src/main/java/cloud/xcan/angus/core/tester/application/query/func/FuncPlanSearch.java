package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncPlanSearch {

  Page<FuncPlan> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncPlan> clz, String... matches);

}




