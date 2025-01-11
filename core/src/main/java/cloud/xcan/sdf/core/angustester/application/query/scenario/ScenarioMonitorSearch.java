package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioMonitorSearch {

  Page<ScenarioMonitor> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<ScenarioMonitor> clz, String... matches);
}
