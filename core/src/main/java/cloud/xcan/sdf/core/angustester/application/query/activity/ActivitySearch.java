package cloud.xcan.sdf.core.angustester.application.query.activity;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ActivitySearch {

  Page<Activity> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<Activity> clz, String... matches);
}
