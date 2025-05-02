package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScenarioTrashSearch {

  Page<ScenarioTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ScenarioTrash> clz, String... matches);

}




