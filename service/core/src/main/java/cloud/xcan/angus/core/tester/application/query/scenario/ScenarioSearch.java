package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScenarioSearch {

  Page<Scenario> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Scenario> clz, String... matches);
}




