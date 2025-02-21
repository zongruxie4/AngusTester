package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScenarioSearch {

  Page<Scenario> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Scenario> clz, String... matches);
}




