package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioMonitorSearch {

  Page<ScenarioMonitor> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ScenarioMonitor> clz, String... matches);
}
