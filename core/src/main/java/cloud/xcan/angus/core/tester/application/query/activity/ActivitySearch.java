package cloud.xcan.angus.core.tester.application.query.activity;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ActivitySearch {

  Page<Activity> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<Activity> clz, String... matches);
}
